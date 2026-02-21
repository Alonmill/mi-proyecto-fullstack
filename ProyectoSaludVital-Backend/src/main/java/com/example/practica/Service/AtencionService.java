package com.example.practica.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.practica.DTO.EntradaHistorialDTO;
import com.example.practica.DTO.RegistrarAtencion;
import com.example.practica.Enum.DiaSemana;
import com.example.practica.Enum.EstadoCita;
import com.example.practica.Model.Cita;
import com.example.practica.Model.EntradaHistorial;
import com.example.practica.Model.ExpedienteMedico;
import com.example.practica.Model.HorarioAtencion;
import com.example.practica.Model.Medico;
import com.example.practica.Model.Paciente;
import com.example.practica.Model.Usuario;
import com.example.practica.Repository.CitaRepository;
import com.example.practica.Repository.EntradaHistorialRepository;
import com.example.practica.Repository.ExpedienteMedicoRepository;
import com.example.practica.Repository.MedicoRepository;
import com.example.practica.Repository.PacienteRepository;
import com.example.practica.Repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AtencionService {

    private final CitaRepository citaRepo;
    private final ExpedienteMedicoRepository expedienteRepo;
    private final EntradaHistorialRepository historialRepo;
    private final PacienteRepository pacienteRepo;
    private final UsuarioRepository usuarioRepo;
    private final MedicoRepository medicoRepo;


    public EntradaHistorialDTO registrarAtencion(Long idPaciente, RegistrarAtencion request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Usuario usuarioActual = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuarioActual.tieneRol("MEDICO")) {
            throw new RuntimeException("No tienes permisos para registrar atenciones");
        }

        // 1. Buscar paciente
        Paciente paciente = pacienteRepo.findById(idPaciente)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        // 2. Buscar cita
        Cita cita = citaRepo.findById(request.getCitaId())
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        Medico medicoActual = medicoRepo.findByUsuario(usuarioActual)
                .orElseThrow(() -> new RuntimeException("El usuario no está registrado como médico"));

        if (!cita.getMedico().getId().equals(medicoActual.getId())) {
            throw new RuntimeException("No puedes registrar la atención de una cita que no te corresponde");
        }

        if (!cita.getPaciente().getId().equals(idPaciente)) {
            throw new RuntimeException("La cita no corresponde al paciente");
        }

        if (cita.getEstado() != EstadoCita.PROGRAMADA) {
            throw new RuntimeException("Solo se pueden atender citas PROGRAMADAS");
        }

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        LocalDateTime fechaHoraCita = cita.getFecha().atTime(cita.getHora());
        LocalDateTime inicioVentana = fechaHoraCita;
        LocalDateTime finVentana = fechaHoraCita.plusHours(2);
        LocalDateTime ahoraDateTime = LocalDateTime.now();

        if (ahoraDateTime.isBefore(inicioVentana)) {
            throw new RuntimeException("La atención solo puede registrarse desde la hora programada de la cita");
        }

        if (ahoraDateTime.isAfter(finVentana)) {
            cita.setEstado(EstadoCita.VENCIDA);
            citaRepo.save(cita);
            throw new RuntimeException("La cita venció. Solo podía atenderse hasta 2 horas después de la hora programada");
        }

        // 🔹 Validación de que la cita está dentro del horario del médico
        DiaSemana diaCita = DiaEnEspañol(cita.getFecha().getDayOfWeek());

        HorarioAtencion horario = cita.getMedico().getHorarios().stream()
                .filter(h -> h.getDia() == diaCita)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("El médico no tiene horario configurado para este día"));

        if (cita.getHora().isBefore(horario.getHoraInicio()) || cita.getHora().isAfter(horario.getHoraFin())) {
            throw new RuntimeException("La hora de la cita no está dentro del horario del médico");
        }

        // 3. Buscar expediente
        ExpedienteMedico expediente = expedienteRepo.findByPaciente(paciente)
                .orElseThrow(() -> new RuntimeException("El paciente no tiene expediente médico"));

        // 4. Crear nueva entrada
        EntradaHistorial entrada = new EntradaHistorial();
        entrada.setFecha(hoy);
        entrada.setHora(ahora);
        entrada.setDiagnostico(request.getDiagnostico());
        entrada.setTratamiento(request.getTratamiento());
        entrada.setExpediente(expediente);
        entrada.setMedico(cita.getMedico());
        entrada.setCita(cita);

        // 5. Crear expediente = atender cita
        cita.setEstado(EstadoCita.ATENDIDA);
        citaRepo.save(cita);

        // 6. Guardar entrada
        EntradaHistorial guardado = historialRepo.save(entrada);

        // 7. Retornar DTO
        return new EntradaHistorialDTO(
                guardado.getId(),
                guardado.getFecha(),
                guardado.getHora(),
                guardado.getDiagnostico(),
                guardado.getTratamiento(),
                guardado.getMedico().getNombre(),
                paciente.getNombre(),
                cita.getEstado()
        );
    }
    
    private DiaSemana DiaEnEspañol(DayOfWeek semanaIngles) {
        return switch (semanaIngles) {
            case MONDAY -> DiaSemana.LUNES;
            case TUESDAY -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY -> DiaSemana.JUEVES;
            case FRIDAY -> DiaSemana.VIERNES;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
}
    


}