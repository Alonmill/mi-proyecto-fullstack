package com.example.practica.Service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.practica.DTO.ActualizarRecetaDTO;
import com.example.practica.DTO.AgregarRecetaDTO;
import com.example.practica.DTO.ItemRecetaDTO;
import com.example.practica.DTO.ObtenerRecetaDTO;
import com.example.practica.Enum.EstadoCita;
import com.example.practica.Enum.EstadoReceta;
import com.example.practica.Model.ItemReceta;
import com.example.practica.Model.Medicamento;
import com.example.practica.Model.Medico;
import com.example.practica.Model.Paciente;
import com.example.practica.Model.Receta;
import com.example.practica.Model.Usuario;
import com.example.practica.Repository.CitaRepository;
import com.example.practica.Repository.MedicamentoRepository;
import com.example.practica.Repository.MedicoRepository;
import com.example.practica.Repository.PacienteRepository;
import com.example.practica.Repository.RecetaRepository;
import com.example.practica.Repository.UsuarioRepository;

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

    public List<ObtenerRecetaDTO> listarRecetas() {
        Usuario usuario = getUsuarioActual();

        List<Receta> recetas = usuario.tieneRol("ADMIN")
                ? recetaRepo.findAll()
                : recetaRepo.findByMedicoUsuarioEmail(usuario.getEmail());

        return recetas.stream()
                .map(this::normalizarEstadoYMapear)
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
                .map(this::normalizarEstadoYMapear)
                .toList();
    }

    public ObtenerRecetaDTO emitirReceta(AgregarRecetaDTO dto) {
        Usuario usuario = getUsuarioActual();

        Medico medico = medicoRepo.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        Paciente paciente = pacienteRepo.findById(dto.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        boolean tieneCitaCompletada = citaRepo.existsByPacienteAndMedicoAndEstado(paciente, medico, EstadoCita.ATENDIDA);

        if (!tieneCitaCompletada) {
            throw new RuntimeException("Solo se pueden emitir recetas si el paciente tiene una cita ATENDIDA con este médico");
        }

        Receta receta = Receta.builder()
                .fechaEmision(LocalDate.now())
                .fechaCaducidad(LocalDate.now().plusDays(30))
                .estado(EstadoReceta.EMITIDA)
                .paciente(paciente)
                .medico(medico)
                .build();

        receta.setItems(crearItems(dto.getItems(), receta, paciente));
        recetaRepo.save(receta);

        return mapToDTO(receta);
    }

    public ObtenerRecetaDTO actualizarReceta(ActualizarRecetaDTO dto) {
        Medico medico = obtenerMedicoActual();

        Receta receta = recetaRepo.findById(dto.getIdReceta())
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        validarPropietario(receta, medico);
        validarEditable(receta);

        receta.getItems().clear();
        receta.getItems().addAll(crearItems(dto.getItems(), receta, receta.getPaciente()));

        recetaRepo.save(receta);

        return mapToDTO(receta);
    }

    public ObtenerRecetaDTO anularReceta(Long idReceta) {
        Medico medico = obtenerMedicoActual();
        Receta receta = recetaRepo.findById(idReceta)
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        validarPropietario(receta, medico);

        if (receta.getEstado() != EstadoReceta.EMITIDA) {
            throw new RuntimeException("Solo se pueden anular recetas en estado EMITIDA");
        }

        receta.setEstado(EstadoReceta.ANULADA);
        recetaRepo.save(receta);
        return mapToDTO(receta);
    }

    public ObtenerRecetaDTO duplicarReceta(Long idReceta) {
        Medico medico = obtenerMedicoActual();
        Receta recetaOriginal = recetaRepo.findById(idReceta)
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        validarPropietario(recetaOriginal, medico);
        normalizarEstado(recetaOriginal);

        if (recetaOriginal.getEstado() != EstadoReceta.EMITIDA) {
            throw new RuntimeException("Solo se pueden duplicar recetas en estado EMITIDA");
        }

        return crearRecetaDesdeExistente(recetaOriginal, medico);
    }

    public ObtenerRecetaDTO renovarReceta(Long idReceta) {
        Medico medico = obtenerMedicoActual();
        Receta recetaOriginal = recetaRepo.findById(idReceta)
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        validarPropietario(recetaOriginal, medico);
        normalizarEstado(recetaOriginal);

        if (recetaOriginal.getEstado() != EstadoReceta.VENCIDA) {
            throw new RuntimeException("Solo se pueden renovar recetas en estado VENCIDA");
        }

        return crearRecetaDesdeExistente(recetaOriginal, medico);
    }

    public ObtenerRecetaDTO verReceta(Long idReceta) {
        Usuario usuarioActual = getUsuarioActual();

        Receta receta = recetaRepo.findById(idReceta)
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        if (usuarioActual.tieneRol("MEDICO") && !receta.getMedico().getUsuario().getId().equals(usuarioActual.getId())) {
            throw new RuntimeException("No puedes ver recetas emitidas por otro médico");
        }

        if (!(usuarioActual.tieneRol("MEDICO") || usuarioActual.tieneRol("ADMIN"))) {
            throw new RuntimeException("No tienes permisos para ver esta receta");
        }

        return normalizarEstadoYMapear(receta);
    }

    private ObtenerRecetaDTO crearRecetaDesdeExistente(Receta original, Medico medico) {
        Receta nueva = Receta.builder()
                .fechaEmision(LocalDate.now())
                .fechaCaducidad(LocalDate.now().plusDays(30))
                .estado(EstadoReceta.EMITIDA)
                .medico(medico)
                .paciente(original.getPaciente())
                .build();

        List<ItemRecetaDTO> itemsDto = original.getItems().stream()
                .map(item -> ItemRecetaDTO.builder()
                        .medicamentoId(item.getMedicamento().getId())
                        .dosis(item.getDosis())
                        .frecuencia(item.getFrecuencia())
                        .build())
                .toList();

        nueva.setItems(crearItems(itemsDto, nueva, original.getPaciente()));
        recetaRepo.save(nueva);
        return mapToDTO(nueva);
    }

    private List<ItemReceta> crearItems(List<ItemRecetaDTO> itemsDto, Receta receta, Paciente paciente) {
        return itemsDto.stream().map(itemDto -> {
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
    }

    private void validarEditable(Receta receta) {
        normalizarEstado(receta);
        if (receta.getEstado() != EstadoReceta.BORRADOR) {
            throw new RuntimeException("Solo se puede editar una receta en estado BORRADOR");
        }
    }

    private void validarPropietario(Receta receta, Medico medico) {
        if (!receta.getMedico().equals(medico)) {
            throw new RuntimeException("No puedes modificar recetas de otro médico");
        }
    }

    private void normalizarEstado(Receta receta) {
        if (receta.getEstado() == EstadoReceta.EMITIDA && receta.getFechaCaducidad() != null
                && receta.getFechaCaducidad().isBefore(LocalDate.now())) {
            receta.setEstado(EstadoReceta.VENCIDA);
            recetaRepo.save(receta);
        }
    }

    private ObtenerRecetaDTO normalizarEstadoYMapear(Receta receta) {
        normalizarEstado(receta);
        return mapToDTO(receta);
    }

    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private Medico obtenerMedicoActual() {
        Usuario usuario = getUsuarioActual();
        if (!usuario.tieneRol("MEDICO")) {
            throw new RuntimeException("No tienes permisos para modificar recetas");
        }
        return medicoRepo.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));
    }

    private ObtenerRecetaDTO mapToDTO(Receta receta) {
        return ObtenerRecetaDTO.builder()
                .id(receta.getId())
                .fechaEmision(receta.getFechaEmision())
                .fechaCaducidad(receta.getFechaCaducidad())
                .medicoNombre(receta.getMedico().getNombre())
                .pacienteNombre(receta.getPaciente().getNombre())
                .estado(receta.getEstado().name())
                .items(
                        receta.getItems().stream()
                                .map(item -> ItemRecetaDTO.builder()
                                        .medicamentoId(item.getMedicamento().getId())
                                        .medicamentoNombre(item.getMedicamento().getNombre())
                                        .dosis(item.getDosis())
                                        .frecuencia(item.getFrecuencia())
                                        .build())
                                .toList())
                .build();
    }
}
