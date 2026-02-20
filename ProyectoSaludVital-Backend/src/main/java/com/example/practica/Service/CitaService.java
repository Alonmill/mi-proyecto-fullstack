package com.example.practica.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.practica.DTO.ActualizarCita;
import com.example.practica.DTO.CitaObtenida;
import com.example.practica.DTO.CrearCita;
import com.example.practica.DTO.ListaCitaPaciente;
import com.example.practica.Enum.DiaSemana;
import com.example.practica.Enum.EstadoCita;
import com.example.practica.Model.Cita;
import com.example.practica.Model.Medico;
import com.example.practica.Model.Paciente;
import com.example.practica.Model.Usuario;
import com.example.practica.Repository.CitaRepository;
import com.example.practica.Repository.MedicoRepository;
import com.example.practica.Repository.PacienteRepository;
import com.example.practica.Repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CitaService {
	
	private final CitaRepository citarepo;
	private final PacienteRepository pacienterepo;
	private final MedicoRepository medicorepo;
	 private final UsuarioRepository usuarioRepository;

	
	 @Transactional
	 public CitaObtenida crearCita(CrearCita citaDTO) {
	     Paciente paciente = pacienterepo.findById(citaDTO.getIdPaciente())
	             .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
	     Medico medico = medicorepo.findById(citaDTO.getIdMedico())
	             .orElseThrow(() -> new IllegalArgumentException("Medico no encontrado"));

	     // Validar máximo de 3 citas por paciente en el mismo día
	     List<Cita> numCitas = citarepo.findByPaciente_IdAndFecha(paciente.getId(), citaDTO.getFecha());
	     if (numCitas.size() >= 3) {
	         throw new IllegalArgumentException("El paciente ya tiene 3 citas en un día");
	     }

	     // Validar que el horario no esté ocupado
	     if (citarepo.existsByMedico_IdAndFechaAndHora(medico.getId(), citaDTO.getFecha(), citaDTO.getHora())) {
	         throw new IllegalArgumentException("El horario ya está ocupado por otro médico");
	     }

	     // Validar que la hora esté dentro del horario del médico
	     DiaSemana diaSemana = DiaEnEspañol(citaDTO.getFecha().getDayOfWeek());
	     boolean dentroHorario = medico.getHorarios().stream().anyMatch(h ->
	             h.getDia() == diaSemana &&
	             !citaDTO.getHora().isBefore(h.getHoraInicio()) &&
	             !citaDTO.getHora().isAfter(h.getHoraFin())
	     );
	     if (!dentroHorario) {
	         throw new IllegalArgumentException("La cita está fuera del horario de atención del médico.");
	     }

	     // Crear y guardar la cita
	     Cita nuevaCita = Cita.builder()
	             .fecha(citaDTO.getFecha())
	             .hora(citaDTO.getHora())
	             .paciente(paciente)
	             .medico(medico)
	             .estado(EstadoCita.PROGRAMADA)
	             .motivo(citaDTO.getMotivo())
	             .tarifaAplicada(medico.getTarifaConsulta())
	             .build();

	     Cita guardada = citarepo.save(nuevaCita);

	     // Convertir la entidad a DTO de salida
	     return new CitaObtenida(
	             guardada.getId(),
	             guardada.getFecha(),
	             guardada.getHora(),
	             guardada.getMotivo(),
	             paciente.getNombre(),  // asegúrate que el campo sea "nombre"
	             medico.getNombre(),
	             medico.getId()// idem aquí
	     );
	 }
	
	 @Transactional
	 public CitaObtenida cancelarCita(long citaId) {
	     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	     String email = auth.getName();

	     Usuario usuario = usuarioRepository.findByEmail(email)
	             .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

	     // Obtengo la cita
	     Cita cita = citarepo.findById(citaId)
	             .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

	     boolean esPacienteDeLaCita = pacienterepo.findByUsuario(usuario)
	             .map(p -> p.getId().equals(cita.getPaciente().getId()))
	             .orElse(false);

	     boolean esAdmin = usuario.tieneRol("ADMIN");

	     // ✅ Solo permitir cancelar si es ADMIN o si es el paciente dueño de la cita
	     if (!(esPacienteDeLaCita || esAdmin)) {
	         throw new RuntimeException("Solo el paciente o un administrador pueden cancelar esta cita");
	     }

	     // ✅ Restricción de cancelación con 2 horas de anticipación
	     LocalDateTime fechaHoraCita = cita.getFecha().atTime(cita.getHora());
	     if (LocalDateTime.now().isAfter(fechaHoraCita.minusHours(2))) {
	         throw new IllegalArgumentException("La cita solo puede cancelarse con al menos 2 horas de anticipación.");
	     }

	     cita.setEstado(EstadoCita.CANCELADA);
	     Cita guardada = citarepo.save(cita);

	     return new CitaObtenida(
	             guardada.getId(),
	             guardada.getFecha(),
	             guardada.getHora(),
	             guardada.getMotivo(),
	             guardada.getPaciente().getNombre(),
	             guardada.getMedico().getNombre(),
	             guardada.getMedico().getId()
	     );
	 }

	 @Transactional
	 public CitaObtenida actualizarCita(long citaId, ActualizarCita actualizarDTO) {
	     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	     String email = auth.getName();

	     Usuario usuario = usuarioRepository.findByEmail(email)
	             .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

	     // Obtengo la cita
	     Cita cita = citarepo.findById(citaId)
	             .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

	     // ❌ Solo permitir actualizar citas PROGRAMADAS
	     if (cita.getEstado() != EstadoCita.PROGRAMADA) {
	         throw new IllegalArgumentException("Solo se pueden actualizar citas PROGRAMADAS");
	     }

	     boolean esPacienteDeLaCita = pacienterepo.findByUsuario(usuario)
	             .map(p -> p.getId().equals(cita.getPaciente().getId()))
	             .orElse(false);

	     boolean esAdmin = usuario.tieneRol("ADMIN");

	     if (!(esPacienteDeLaCita || esAdmin)) {
	         throw new RuntimeException("No tienes permisos para modificar esta cita");
	     }

	     // Validar límite de tiempo (mínimo 2 horas antes)
	     LocalDateTime FechaHoraOriginal = cita.getFecha().atTime(cita.getHora());
	     if (LocalDateTime.now().isAfter(FechaHoraOriginal.minusHours(2))) {
	         throw new IllegalArgumentException("La modificación solo se puede hacer con 2 horas de anticipación");
	     }

	     // 🔎 Verificar si realmente cambió médico, fecha o hora
	     boolean cambioMedico = !cita.getMedico().getId().equals(actualizarDTO.getIdMedico());
	     boolean cambioFecha = !cita.getFecha().equals(actualizarDTO.getFecha());
	     boolean cambioHora = !cita.getHora().equals(actualizarDTO.getHora());

	     if (cambioMedico || cambioFecha || cambioHora) {
	         Medico medicoNue = medicorepo.findById(actualizarDTO.getIdMedico())
	                 .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));

	         long citasPacienteXDia = citarepo.countByPaciente_IdAndFechaAndEstado(
	                 cita.getPaciente().getId(),
	                 actualizarDTO.getFecha(),
	                 EstadoCita.PROGRAMADA
	         );

	         if (cita.getFecha().equals(actualizarDTO.getFecha()) && cita.getEstado() == EstadoCita.PROGRAMADA) {
	             citasPacienteXDia = Math.max(0, citasPacienteXDia - 1);
	         }

	         if (citasPacienteXDia >= 3) {
	             throw new IllegalArgumentException("El paciente ya tiene 3 citas programadas para ese día");
	         }

	         boolean medicoOcupado = citarepo.existsByMedico_IdAndFechaAndHoraAndIdNot(
	                 medicoNue.getId(),
	                 actualizarDTO.getFecha(),
	                 actualizarDTO.getHora(),
	                 cita.getId()
	         );
	         if (medicoOcupado) {
	             throw new IllegalArgumentException("El médico ya tiene una cita en ese horario");
	         }

	         DiaSemana diaSemana = DiaEnEspañol(actualizarDTO.getFecha().getDayOfWeek());
	         boolean dentroHorario = medicoNue.getHorarios().stream().anyMatch(h ->
	                 h.getDia() == diaSemana &&
	                 !actualizarDTO.getHora().isBefore(h.getHoraInicio()) &&
	                 !actualizarDTO.getHora().isAfter(h.getHoraFin())
	         );
	         if (!dentroHorario) {
	             throw new IllegalArgumentException("La cita está fuera del horario de atención del médico.");
	         }

	         // ✅ Actualizar solo si cambió médico/fecha/hora
	         cita.setMedico(medicoNue);
	         cita.setFecha(actualizarDTO.getFecha());
	         cita.setHora(actualizarDTO.getHora());
	         cita.setTarifaAplicada(medicoNue.getTarifaConsulta());
	     }

	     // ✅ Actualizar motivo si se envía
	     if (actualizarDTO.getMotivo() != null && !actualizarDTO.getMotivo().isBlank()) {
	         cita.setMotivo(actualizarDTO.getMotivo());
	     }

	     Cita guardada = citarepo.save(cita);

	     return new CitaObtenida(
	             guardada.getId(),
	             guardada.getFecha(),
	             guardada.getHora(),
	             guardada.getMotivo(),
	             guardada.getPaciente().getNombre(),
	             guardada.getMedico().getNombre(),
	             guardada.getMedico().getId()
	     );
	 }

	 
	 @Transactional
	 public List<CitaObtenida> listarCitasProgramadas() {
	     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	     String email = auth.getName();

	     Usuario usuario = usuarioRepository.findByEmail(email)
	             .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

	     boolean esAdmin = usuario.tieneRol("ADMIN");

	     List<Cita> citas;
	     if (esAdmin) {
	         citas = citarepo.findByEstado(EstadoCita.PROGRAMADA); // solo PROGRAMADAS
	     } else {
	         citas = citarepo.findByPaciente_UsuarioAndEstado(usuario, EstadoCita.PROGRAMADA);
	     }

	     return citas.stream()
	             .map(c -> new CitaObtenida(
	                     c.getId(),
	                     c.getFecha(),
	                     c.getHora(),
	                     c.getMotivo(),
	                     c.getPaciente().getNombre(),
	                     c.getMedico().getNombre(),
	                     c.getMedico().getId()
	             ))
	             .collect(Collectors.toList());
	 }

	
	
	 public List<ListaCitaPaciente> obtenerCitasPacienteActual() {
		    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		    String email = auth.getName();

		    Usuario usuario = usuarioRepository.findByEmail(email)
		            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		    Paciente paciente = pacienterepo.findByUsuario(usuario)
		            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

		    List<Cita> citas = citarepo.findByPaciente_Id(paciente.getId());

		    return citas.stream()
		            .map(c -> {
		            	ListaCitaPaciente dto = new ListaCitaPaciente();
		                dto.setId(c.getId());
		                dto.setFecha(c.getFecha());
		                dto.setHora(c.getHora());
		                dto.setMotivo(c.getMotivo());
		                dto.setPacienteNombre(c.getPaciente().getNombre());
		                dto.setMedicoNombre(c.getMedico().getNombre());
		                dto.setTarifa(c.getTarifaAplicada());
		                dto.setEstado(c.getEstado().name());
		                return dto;
		            })
		            .toList();
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
