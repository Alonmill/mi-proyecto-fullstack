package com.example.practica.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.practica.Repository.ExpedienteMedicoRepository;
import com.example.practica.Repository.MedicoRepository;
import com.example.practica.Repository.UsuarioRepository;
import com.example.practica.DTO.*;
import com.example.practica.Model.EntradaHistorial;
import com.example.practica.Model.ExpedienteMedico;
import com.example.practica.Model.Medico;
import com.example.practica.Model.Paciente;
import com.example.practica.Model.Usuario;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpedienteService {

    private final ExpedienteMedicoRepository expedienteRepo;
    private final UsuarioRepository usuarioRepo;
    private final MedicoRepository medicoRepo;

    // Listado de todos los expedientes (ADMIN y MEDICO)
    public List<ExpedienteMedicoDTO> listarExpedientes() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ADMIN"));

        List<ExpedienteMedico> expedientes;

        if (esAdmin) {
            expedientes = expedienteRepo.findAll();
        } else {
            expedientes = expedienteRepo.findExpedientesPorMedico(email); // usa el query correcto
        }

        return expedientes.stream()
                .map(exp -> new ExpedienteMedicoDTO(
                        exp.getId(),
                        exp.getPaciente().getNombre(),
                        exp.getPaciente().getNumeroIdentificacion(),
                        exp.getPaciente().getFechaNacimiento(),
                        obtenerNombreMedicoPrincipal(exp),
                        exp.getEntradas().stream()
                            .map(entrada -> new EntradaHistorialDTO(
                                    entrada.getId(),
                                    entrada.getFecha(),
                                    entrada.getHora(),
                                    entrada.getDiagnostico(),
                                    entrada.getTratamiento(),
                                    entrada.getMedico().getNombre(),
                                    exp.getPaciente().getNumeroIdentificacion(),
                                    entrada.getCita() != null ? entrada.getCita().getEstado() : null
                            ))
                            .toList()
                ))
                .toList();
    }




    private String obtenerNombreMedicoPrincipal(ExpedienteMedico expediente) {
        return expediente.getEntradas().stream()
                .findFirst()
                .map(e -> e.getMedico().getNombre())
                .orElse("Sin médico asignado");
    }

    // Ver expediente por ID (PACIENTE y MEDICO)
    public ExpedienteMedicoDTO verExpediente(Long id) {
        ExpedienteMedico expediente = expedienteRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Expediente no encontrado"));

        // Validar que un PACIENTE solo vea su propio expediente
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean esPaciente = usuario.tieneRol("PACIENTE") && expediente.getPaciente().getUsuario().getId().equals(usuario.getId());

        boolean esMedico = false;
        if (usuario.tieneRol("MEDICO")) {
            Medico medico = medicoRepo.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

            // Verificar que el médico tenga al menos una entrada en este expediente
            esMedico = expediente.getEntradas().stream()
                    .anyMatch(eh -> eh.getMedico().getId().equals(medico.getId()));
        }

        if (!esPaciente && !esMedico) {
            throw new RuntimeException("No tiene permisos para ver este expediente");
        }

        return new ExpedienteMedicoDTO(
                expediente.getId(),
                expediente.getPaciente().getNombre(),
                expediente.getPaciente().getNumeroIdentificacion(),
                expediente.getPaciente().getFechaNacimiento(),
                obtenerNombreMedicoPrincipal(expediente),
                expediente.getEntradas().stream()
                        .map(entrada -> new EntradaHistorialDTO(
                                entrada.getId(),
                                entrada.getFecha(),
                                entrada.getHora(),
                                entrada.getDiagnostico(),
                                entrada.getTratamiento(),
                                entrada.getMedico().getNombre(),
                                expediente.getPaciente().getNumeroIdentificacion(),
                                entrada.getCita().getEstado()
                        ))
                        .toList()
        );
    }
    
    public ExpedienteMedicoDTO editarExpediente(Long id, RegistrarAtencion request) {
        ExpedienteMedico expediente = expedienteRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Expediente no encontrado"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean esAdmin = usuario.tieneRol("ADMIN");
        boolean esMedico = false;
        if (usuario.tieneRol("MEDICO")) {
            Medico medico = medicoRepo.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

            esMedico = expediente.getEntradas().stream()
                    .anyMatch(eh -> eh.getMedico().getId().equals(medico.getId()));
        }

        if (!esAdmin && !esMedico) {
            throw new RuntimeException("No tienes permisos para editar este expediente");
        }

        // 🔎 Validar que la citaId pertenezca a este expediente
        EntradaHistorial entrada = expediente.getEntradas().stream()
                .filter(eh -> eh.getCita() != null && eh.getCita().getId().equals(request.getCitaId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("La cita no corresponde a este expediente"));

        // ✅ Editar solo esa entrada vinculada a la cita correcta
        entrada.setDiagnostico(request.getDiagnostico());
        entrada.setTratamiento(request.getTratamiento());

        ExpedienteMedico guardado = expedienteRepo.save(expediente);

        return new ExpedienteMedicoDTO(
                guardado.getId(),
                guardado.getPaciente().getNombre(),
                guardado.getPaciente().getNumeroIdentificacion(),
                guardado.getPaciente().getFechaNacimiento(),
                obtenerNombreMedicoPrincipal(guardado),
                guardado.getEntradas().stream()
                        .map(eh -> new EntradaHistorialDTO(
                                eh.getId(),
                                eh.getFecha(),
                                eh.getHora(),
                                eh.getDiagnostico(),
                                eh.getTratamiento(),
                                eh.getMedico().getNombre(),
                                guardado.getPaciente().getNumeroIdentificacion(),
                                eh.getCita().getEstado()
                        ))
                        .toList()
        );
    }


    
}
