package com.example.practica.Service;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.practica.DTO.AgregarPaciente;
import com.example.practica.DTO.EntradaHistorialDTO;
import com.example.practica.DTO.ExpedienteMedicoDTO;
import com.example.practica.DTO.PacienteObtenido;
import com.example.practica.Model.Alergia;
import com.example.practica.Model.Enfermedad;
import com.example.practica.Model.ExpedienteMedico;
import com.example.practica.Model.Medico;
import com.example.practica.Model.Paciente;
import com.example.practica.Model.Usuario;
import com.example.practica.Repository.ExpedienteMedicoRepository;
import com.example.practica.Repository.MedicoRepository;
import com.example.practica.Repository.PacienteRepository;
import com.example.practica.Repository.RoleRepository;
import com.example.practica.Repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PacienteService {

    private final PacienteRepository pacirepo;
    private final ExpedienteMedicoRepository expedienteRepo;
    private final UsuarioRepository usuarioRepo;
    private final MedicoRepository medicoRepo;
    private final RoleRepository roleRepo;


    public List<PacienteObtenido> listarPacientes() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ADMIN"));

        List<Paciente> pacientes;

        if (esAdmin) {
            pacientes = pacirepo.findAll();
        } else {
            pacientes = pacirepo.findPacientesAtendidosPorMedico(email);
        }

        return pacientes.stream()
                .map(this::convertirAPacienteObtenido)
                .toList();
    }


    // Método auxiliar para convertir Paciente a PacienteObtenido
    private PacienteObtenido convertirAPacienteObtenido(Paciente paciente) {
        List<String> nombresAlergias = paciente.getAlergias().stream()
                .map(Alergia::getNombre)
                .toList();
        List<String> nombresEnfermedades = paciente.getEnfermedades().stream()
                .map(Enfermedad::getNombre)
                .toList();

        return new PacienteObtenido(
                paciente.getId(),
                paciente.getNombre(),
                paciente.getNumeroIdentificacion(),
                paciente.getFechaNacimiento(),
                nombresAlergias,
                nombresEnfermedades,
                paciente.getUsuario().getId(),
                paciente.getImagenUrl()
        );
    }
    


    public PacienteObtenido agregarPaciente(AgregarPaciente pacienteDTO) {
    	 Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         String email = auth.getName();

         Usuario usuarioActual = usuarioRepo.findByEmail(email)
                 .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

         if (!usuarioActual.tieneRol("ADMIN")) {
             throw new RuntimeException("No tienes permisos para agregar paciente");
         }

        if (pacirepo.existsByNumeroIdentificacion(pacienteDTO.getNumeroIdentificacion())) {
            throw new IllegalArgumentException("El número de documento ya existe");
        }

        LocalDate fechaNaci = pacienteDTO.getFechaNacimiento();
        if (fechaNaci == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }

        if (pacienteDTO.getAlergias() == null || pacienteDTO.getAlergias().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos una alergia (si no tiene, usar 'Ninguna')");
        }
        

        Usuario usu = usuarioRepo.findById(pacienteDTO.getUsuarioId())
                  .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usu.setRoles(new HashSet<>(Collections.singleton(
                roleRepo.findByName("PACIENTE")
                        .orElseThrow(() -> new RuntimeException("Rol PACIENTE no encontrado"))
        )));
        usu = usuarioRepo.save(usu);

        // Crear paciente
        Paciente pacienteNuevo = Paciente.builder()
                .numeroIdentificacion(pacienteDTO.getNumeroIdentificacion())
                .fechaNacimiento(fechaNaci)
                .nombre(pacienteDTO.getNombre())
                .imagenUrl(pacienteDTO.getImagenUrl())
                .usuario(usu)
                .build();

        // Agregar alergias
        List<Alergia> alergias = new ArrayList<>(pacienteDTO.getAlergias().stream()
                .map(nombre -> Alergia.builder()
                        .nombre(nombre)
                        .paciente(pacienteNuevo)
                        .build())
                .toList());

        pacienteNuevo.setAlergias(alergias);
        
     // Agregar Enfermedades
        List<Enfermedad> enfermedades = new ArrayList<>(pacienteDTO.getEnfermedades().stream()
                .map(nombre -> Enfermedad.builder()
                        .nombre(nombre)
                        .paciente(pacienteNuevo)
                        .build())
                .toList());

        pacienteNuevo.setEnfermedades(enfermedades);

        Paciente guardado = pacirepo.save(pacienteNuevo);

        // Crear expediente médico
        ExpedienteMedico expediente = ExpedienteMedico.builder()
                .paciente(guardado)
                .entradas(new ArrayList<>())
                .build();

        expedienteRepo.save(expediente);

        List<String> nombresAlergias = guardado.getAlergias().stream()
                .map(Alergia::getNombre)
                .toList();
        List<String> nombresEnfermedades = guardado.getEnfermedades().stream()
                .map(Enfermedad::getNombre)
                .toList();

        return new PacienteObtenido(
                guardado.getId(),
                guardado.getNombre(),
                guardado.getNumeroIdentificacion(),
                guardado.getFechaNacimiento(),
                nombresAlergias,
                nombresEnfermedades,
                guardado.getUsuario().getId(),
                guardado.getImagenUrl()
        );
    }


    public PacienteObtenido editarPaciente(Long id, AgregarPaciente pacienteDTO) {
    	 Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         String email = auth.getName();

         Usuario usuarioActual = usuarioRepo.findByEmail(email)
                 .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

         Paciente paciente = pacirepo.findById(id)
                 .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

         boolean esAdmin = usuarioActual.tieneRol("ADMIN");
         boolean esMismoPaciente = paciente.getUsuario() != null &&
        		 paciente.getUsuario().getId().equals(usuarioActual.getId());

         if (!(esAdmin || esMismoPaciente)) {
             throw new RuntimeException("No tienes permisos para actualizar este médico");
         }
        
        paciente.setNombre(pacienteDTO.getNombre());
        paciente.setNumeroIdentificacion(pacienteDTO.getNumeroIdentificacion());
        paciente.setFechaNacimiento(pacienteDTO.getFechaNacimiento());
        paciente.setImagenUrl(pacienteDTO.getImagenUrl());

        // Actualizar alergias: eliminamos las anteriores y agregamos las nuevas
        paciente.getAlergias().clear();
        List<Alergia> alergias = pacienteDTO.getAlergias().stream()
                .map(nombre -> Alergia.builder()
                        .nombre(nombre)
                        .paciente(paciente)
                        .build())
                .toList();
        paciente.getAlergias().addAll(alergias);
        
     // Actualizar enfermedades: eliminamos las anteriores y agregamos las nuevas
        paciente.getEnfermedades().clear();
        List<Enfermedad> enfermedades = pacienteDTO.getEnfermedades().stream()
                .map(nombre -> Enfermedad.builder()
                        .nombre(nombre)
                        .paciente(paciente)
                        .build())
                .toList();
        paciente.getEnfermedades().addAll(enfermedades);
        
        
        if (pacienteDTO.getUsuarioId() != null) {
            Usuario usuario = usuarioRepo.findById(pacienteDTO.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            paciente.setUsuario(usuario);
        }

        Paciente guardado = pacirepo.save(paciente);

        List<String> nombresAlergias = guardado.getAlergias().stream()
                .map(Alergia::getNombre)
                .toList();
        List<String> nombresEnfermedades = guardado.getEnfermedades().stream()
                .map(Enfermedad::getNombre)
                .toList();

        return new PacienteObtenido(
        		 guardado.getId(),
                 guardado.getNombre(),
                 guardado.getNumeroIdentificacion(),
                 guardado.getFechaNacimiento(),
                 nombresAlergias,
                 nombresEnfermedades,
                 guardado.getUsuario().getId(),
                 guardado.getImagenUrl()
        );
    }


    public PacienteObtenido verPerfil() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Paciente paciente = pacirepo.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        List<String> nombresAlergias = paciente.getAlergias().stream()
                .map(Alergia::getNombre)
                .toList();

        List<String> nombresEnfermedades = paciente.getEnfermedades().stream()
                .map(Enfermedad::getNombre)
                .toList();

        return new PacienteObtenido(
                paciente.getId(),
                paciente.getNombre(),
                paciente.getNumeroIdentificacion(),
                paciente.getFechaNacimiento(),
                nombresAlergias,
                nombresEnfermedades,
                paciente.getUsuario().getId(),
                paciente.getImagenUrl()
        );
    }

    public ExpedienteMedicoDTO verExpediente() {
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Paciente paciente = pacirepo.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        
        
        ExpedienteMedico expediente = expedienteRepo.findByPaciente(paciente)
            	.orElseThrow(() -> new RuntimeException("Expediente no encontrado"));

        // Mapear a DTO
        List<EntradaHistorialDTO> entradas = expediente.getEntradas().stream()
                .map(e -> new EntradaHistorialDTO(
                        e.getId(),
                        e.getFecha(),
                        e.getHora(),
                        e.getDiagnostico(),
                        e.getTratamiento(),
                        e.getMedico().getNombre(),
                        e.getExpediente().getPaciente().getNumeroIdentificacion(),
                        e.getCita() != null ? e.getCita().getEstado() : null
                ))
                .toList();

        return new ExpedienteMedicoDTO(
                expediente.getId(),
                expediente.getPaciente().getNombre(),
                expediente.getPaciente().getNumeroIdentificacion(),
                expediente.getPaciente().getFechaNacimiento(),
                entradas.stream().findFirst().map(EntradaHistorialDTO::getNombreMedico).orElse("Sin médico asignado"),
                entradas
        );

        
    }
}


