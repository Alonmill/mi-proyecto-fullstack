package com.example.practica.Service;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.practica.DTO.ActualizarMedicoDTO;
import com.example.practica.DTO.AgregarMedicoDTO;
import com.example.practica.DTO.HorarioDTO;
import com.example.practica.DTO.ObtenerMedicoDTO;
import com.example.practica.Enum.EstadoDoctor;
import com.example.practica.Model.Alergia;
import com.example.practica.Model.HorarioAtencion;
import com.example.practica.Model.Medico;
import com.example.practica.Model.Usuario;
import com.example.practica.Repository.MedicoRepository;
import com.example.practica.Repository.RoleRepository;
import com.example.practica.Repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicoService {

    private final MedicoRepository medicoRepo;
    private final UsuarioRepository usuarioRepo;
    private final RoleRepository roleRepo;
    private final ImageStorageService imageStorageService;

    // Listado de medicos - solo ADMIN
    public List<ObtenerMedicoDTO> listarMedicos() {
        return medicoRepo.findAll()
                .stream()
                .map(m -> {
                    List<HorarioDTO> horariosDTO = m.getHorarios().stream()
                            .map(h -> new HorarioDTO(
                                    h.getDia(),           // si DiaSemana es enum
                                    h.getHoraInicio().toString(),
                                    h.getHoraFin().toString()
                            ))
                            .toList();

                    return new ObtenerMedicoDTO(
                            m.getId(),
                            m.getNombre(),
                            m.getApellido(),
                            m.getNumeroLicencia(),
                            m.getTelefono(),
                            m.getEmail(),
                            m.getEspecialidad(),
                            m.getEstado(),
                            m.isDisponible(),
                            m.getTarifaConsulta(),
                            m.getUsuario() != null ? m.getUsuario().getId() : null,
                            m.getImagenUrl(),
                            horariosDTO
                    );
                })
                .toList();
    }

    // Método para agregar un nuevo médico (solo ADMIN)
    public ObtenerMedicoDTO agregarMedico(AgregarMedicoDTO request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuarioActual = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuarioActual.tieneRol("ADMIN")) {
            throw new RuntimeException("No tienes permisos para agregar médicos");
        }

        Medico medicoNuevo = new Medico();
        medicoNuevo.setNombre(request.getNombre());
        medicoNuevo.setApellido(request.getApellido());
        medicoNuevo.setNumeroLicencia(request.getNumeroLicencia());
        medicoNuevo.setTelefono(request.getTelefono());
        medicoNuevo.setEmail(request.getEmail());
        medicoNuevo.setEspecialidad(request.getEspecialidad());
        medicoNuevo.setEstado(EstadoDoctor.ACTIVO);
        medicoNuevo.setDisponible(true);
        medicoNuevo.setTarifaConsulta(request.getTarifaConsulta());
        medicoNuevo.setImagenUrl(request.getImagenUrl());

        // Relacionar usuario si se proporciona
        if (request.getUsuarioId() != null) {
            Usuario usuario = usuarioRepo.findById(request.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            usuario.setRoles(new HashSet<>(Collections.singleton(
                    roleRepo.findByName("MEDICO")
                            .orElseThrow(() -> new RuntimeException("Rol MEDICO no encontrado"))
            )));

            medicoNuevo.setUsuario(usuarioRepo.save(usuario));
        }

        // Agregar horarios
        List<HorarioAtencion> horarios = request.getHorarios().stream()
                .map(h -> {
                    HorarioAtencion horario = new HorarioAtencion();
                    horario.setDia(h.getDia());
                    horario.setHoraInicio(LocalTime.parse(h.getHoraInicio()));
                    horario.setHoraFin(LocalTime.parse(h.getHoraFin()));
                    horario.setMedico(medicoNuevo);
                    return horario;
                })
                .toList();

        medicoNuevo.setHorarios(horarios);

        Medico guardado = medicoRepo.save(medicoNuevo);
        return mapToDTO(guardado);
    }

    // Método para actualizar un médico (solo el mismo MEDICO o ADMIN)
    public ObtenerMedicoDTO actualizarMedico(Long medicoId, ActualizarMedicoDTO request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuarioActual = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Medico medico = medicoRepo.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        boolean esAdmin = usuarioActual.tieneRol("ADMIN");
      

        if (!(esAdmin)) {
            throw new RuntimeException("No tienes permisos para actualizar este médico");
        }

        // Actualizar los campos permitidos
        medico.setNombre(request.getNombre());
        medico.setApellido(request.getApellido());
        medico.setNumeroLicencia(request.getNumeroLicencia());
        medico.setTelefono(request.getTelefono());
        medico.setEmail(request.getEmail());
        medico.setEspecialidad(request.getEspecialidad());
        medico.setEstado(request.getEstado());
        if (request.getDisponible() != null) {
            medico.setDisponible(request.getDisponible());
        }
        medico.setTarifaConsulta(request.getTarifaConsulta());
        String imagenAnterior = medico.getImagenUrl();
        if (request.getImagenUrl() != null && !request.getImagenUrl().isBlank()) {
            if (imagenAnterior != null && !imagenAnterior.isBlank() && !imagenAnterior.equals(request.getImagenUrl())) {
                imageStorageService.deleteIfManaged(imagenAnterior);
            }
            medico.setImagenUrl(request.getImagenUrl());
        }
        if (request.getUsuarioId() != null) {
            Usuario usuario = usuarioRepo.findById(request.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            medico.setUsuario(usuario);
        }
        
     // Actualizar alergias: eliminamos las anteriores y agregamos las nuevas
        medico.getHorarios().clear();
        List<HorarioAtencion> horarios = request.getHorarios().stream()
                .map(h -> {
                    HorarioAtencion horario = new HorarioAtencion();
                    horario.setDia(h.getDia());
                    horario.setHoraInicio(LocalTime.parse(h.getHoraInicio()));
                    horario.setHoraFin(LocalTime.parse(h.getHoraFin()));
                    horario.setMedico(medico);
                    return horario;
                })
                .toList();
        medico.getHorarios().addAll(horarios);


        Medico guardado = medicoRepo.save(medico);
        return mapToDTO(guardado);
    }


    

    public ObtenerMedicoDTO actualizarPerfil(ActualizarMedicoDTO request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Medico medico = medicoRepo.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Medico no encontrado"));

        medico.setNombre(request.getNombre());
        medico.setApellido(request.getApellido());
        medico.setNumeroLicencia(request.getNumeroLicencia());
        medico.setTelefono(request.getTelefono());
        medico.setEmail(request.getEmail());
        medico.setEspecialidad(request.getEspecialidad());
        medico.setTarifaConsulta(request.getTarifaConsulta());
        String imagenAnterior = medico.getImagenUrl();
        if (request.getImagenUrl() != null && !request.getImagenUrl().isBlank()) {
            if (imagenAnterior != null && !imagenAnterior.isBlank() && !imagenAnterior.equals(request.getImagenUrl())) {
                imageStorageService.deleteIfManaged(imagenAnterior);
            }
            medico.setImagenUrl(request.getImagenUrl());
        }

        if (request.getHorarios() != null) {
            medico.getHorarios().clear();
            List<HorarioAtencion> horarios = request.getHorarios().stream()
                    .map(h -> {
                        HorarioAtencion horario = new HorarioAtencion();
                        horario.setDia(h.getDia());
                        horario.setHoraInicio(LocalTime.parse(h.getHoraInicio()));
                        horario.setHoraFin(LocalTime.parse(h.getHoraFin()));
                        horario.setMedico(medico);
                        return horario;
                    })
                    .toList();
            medico.getHorarios().addAll(horarios);
        }

        Medico guardado = medicoRepo.save(medico);
        return mapToDTO(guardado);
    }

    public ObtenerMedicoDTO verPerfil() {
    	 Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		    String email = auth.getName();

		    Usuario usuario = usuarioRepo.findByEmail(email)
		            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		    Medico medico = medicoRepo.findByUsuario(usuario)
		            .orElseThrow(() -> new RuntimeException("Medico no encontrado"));
        return mapToDTO(medico);
    }

    private ObtenerMedicoDTO mapToDTO(Medico m) {
    	List<HorarioDTO> horariosDTO = m.getHorarios().stream()
    	        .map(h -> new HorarioDTO(
    	                h.getDia(),
    	                h.getHoraInicio().toString(),
    	                h.getHoraFin().toString()
    	        ))
    	        .toList();
    	
    	return new ObtenerMedicoDTO(
    	        m.getId(),
    	        m.getNombre(),
    	        m.getApellido(),
    	        m.getNumeroLicencia(),
    	        m.getTelefono(),
    	        m.getEmail(),
    	        m.getEspecialidad(),
    	        m.getEstado(),
    	        m.isDisponible(),
    	        m.getTarifaConsulta(),
    	        m.getUsuario().getId(),
    	        m.getImagenUrl(),
    	        horariosDTO
    	);
    }
}
