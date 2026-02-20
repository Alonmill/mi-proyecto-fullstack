package com.example.practica.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.practica.DTO.ActualizarRecetaDTO;
import com.example.practica.DTO.AgregarRecetaDTO;
import com.example.practica.DTO.ItemRecetaDTO;
import com.example.practica.DTO.ObtenerRecetaDTO;
import com.example.practica.Enum.EstadoCita;
import com.example.practica.Repository.CitaRepository;
import com.example.practica.Repository.MedicamentoRepository;
import com.example.practica.Repository.MedicoRepository;
import com.example.practica.Repository.PacienteRepository;
import com.example.practica.Repository.RecetaRepository;
import com.example.practica.Repository.UsuarioRepository;
import com.example.practica.Model.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecetaService {

    private final RecetaRepository recetaRepo;
    private final UsuarioRepository usuarioRepo;
    private final PacienteRepository pacienteRepo;
    private final MedicoRepository medicoRepo;
    private final MedicamentoRepository medicamentoRepo;
    private final CitaRepository citaRepo;

   
 // En tu RecetaService o el Service correspondiente
    public List<ObtenerRecetaDTO> listarRecetas() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ADMIN"));

        List<Receta> recetas;

        if (esAdmin) {
            // Admin ve todas
            recetas = recetaRepo.findAll();
        } else {
            // Médico ve solo las que él emitió
            recetas = recetaRepo.findByMedicoUsuarioEmail(email);
        }

        return recetas.stream()
                .map(this::mapToDTO)
                .toList();
    }



    public List<ObtenerRecetaDTO> verMisRecetas() {
        Usuario usuarioActual = getUsuarioActual();

        if (!usuarioActual.tieneRol("PACIENTE")) {
            throw new RuntimeException("No tienes permisos para ver recetas de paciente");
        }

        Paciente paciente = pacienteRepo.findByUsuario(usuarioActual)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        return recetaRepo.findByPaciente(paciente)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    

    public ObtenerRecetaDTO emitirReceta(AgregarRecetaDTO dto) {
        // 🔹 Usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 🔹 Verificar que es médico
        Medico medico = medicoRepo.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        // 🔹 Paciente desde el DTO
        Paciente paciente = pacienteRepo.findById(dto.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        // 🔹 Validar que exista al menos una cita COMPLETADA entre el paciente y el médico autenticado
        boolean tieneCitaCompletada = citaRepo.existsByPacienteAndMedicoAndEstado(paciente, medico, EstadoCita.COMPLETADA);

        if (!tieneCitaCompletada) {
            throw new RuntimeException("Solo se pueden emitir recetas si el paciente tiene una cita COMPLETADA con este médico");
        }

        // 🔹 Crear receta
        Receta receta = Receta.builder()
                .fechaEmision(LocalDate.now())
                .fechaCaducidad(LocalDate.now().plusDays(30))
                .paciente(paciente)
                .medico(medico)
                .build();

        // 🔹 Items de receta
        List<ItemReceta> items = dto.getItems().stream().map(itemDto -> {
            Medicamento medicamento = medicamentoRepo.findById(itemDto.getMedicamentoId())
                    .orElseThrow(() -> new RuntimeException("Medicamento no encontrado"));

            boolean alergico = paciente.getAlergias().stream()
                    .anyMatch(alergia -> alergia.getNombre().equalsIgnoreCase(medicamento.getNombre()));
            if (alergico) {
                throw new RuntimeException("El paciente es alérgico a " + medicamento.getNombre());
            }

            return ItemReceta.builder()
                    .dosis(itemDto.getDosis())
                    .frecuencia(itemDto.getFrecuencia())
                    .medicamento(medicamento)
                    .receta(receta)
                    .build();
        }).toList();

        receta.setItems(items);
        recetaRepo.save(receta);

        return mapToDTO(receta);
    }

    
    public ObtenerRecetaDTO actualizarReceta(ActualizarRecetaDTO dto) {
        // 🔹 Usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 🔹 Verificar que es médico
        boolean esMedico = usuario.tieneRol("MEDICO");
        if (!esMedico) {
            throw new RuntimeException("No tienes permisos para actualizar recetas");
        }

        // 🔹 Obtener el médico a partir del usuario autenticado
        Medico medico = medicoRepo.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        // 🔹 Obtener la receta existente
        Receta receta = recetaRepo.findById(dto.getIdReceta())
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        // 🔹 Verificar que el médico que actualiza es el mismo que creó la receta
        if (!receta.getMedico().equals(medico)) {
            throw new RuntimeException("No puedes actualizar recetas de otro médico");
        }

        // 🔹 Obtener el paciente
        Paciente paciente = receta.getPaciente();

        // 🔹 Crear los nuevos items
        List<ItemReceta> items = dto.getItems().stream().map(itemDto -> {
            Medicamento medicamento = medicamentoRepo.findById(itemDto.getMedicamentoId())
                    .orElseThrow(() -> new RuntimeException("Medicamento no encontrado"));

            boolean alergico = paciente.getAlergias().stream()
                    .anyMatch(alergia -> alergia.getNombre().equalsIgnoreCase(medicamento.getNombre()));
            if (alergico) {
                throw new RuntimeException("El paciente es alérgico a " + medicamento.getNombre());
            }

            return ItemReceta.builder()
                    .dosis(itemDto.getDosis())
                    .frecuencia(itemDto.getFrecuencia())
                    .medicamento(medicamento)
                    .receta(receta)
                    .build();
        }).toList();

        // 🔹 Reemplazar los items antiguos por los nuevos sin romper la referencia
        receta.getItems().clear();
        receta.getItems().addAll(items);

        // 🔹 Guardar cambios
        recetaRepo.save(receta);

        return mapToDTO(receta);
    }




    public ObtenerRecetaDTO verReceta(Long idReceta) {
        Usuario usuarioActual = getUsuarioActual();

        Receta receta = recetaRepo.findById(idReceta)
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        // Si es MEDICO, solo puede ver sus propias recetas
        if (usuarioActual.tieneRol("MEDICO")) {
            if (!receta.getMedico().getUsuario().getId().equals(usuarioActual.getId())) {
                throw new RuntimeException("No puedes ver recetas emitidas por otro médico");
            }
        }

        // Si es ADMIN, puede ver cualquier receta (no validamos nada extra)
        if (!(usuarioActual.tieneRol("MEDICO") || usuarioActual.tieneRol("ADMIN"))) {
            throw new RuntimeException("No tienes permisos para ver esta receta");
        }

        return mapToDTO(receta);
    }


    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private ObtenerRecetaDTO mapToDTO(Receta receta) {
        return ObtenerRecetaDTO.builder()
                .id(receta.getId())
                .fechaEmision(receta.getFechaEmision())
                .fechaCaducidad(receta.getFechaCaducidad())
                .medicoNombre(receta.getMedico().getNombre())
                .pacienteNombre(receta.getPaciente().getNombre())
                .items(
                        receta.getItems().stream()
                                .map(item -> ItemRecetaDTO.builder()
                                        .medicamentoId(item.getMedicamento().getId())
                                        .medicamentoNombre(item.getMedicamento().getNombre())
                                        .dosis(item.getDosis())
                                        .frecuencia(item.getFrecuencia())
                                        .build()
                                ).toList()
                )
                .build();
    }
}