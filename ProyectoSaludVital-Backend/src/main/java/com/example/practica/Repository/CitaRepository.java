package com.example.practica.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.practica.Model.Cita;
import com.example.practica.Model.Medico;
import com.example.practica.Model.Paciente;
import com.example.practica.Model.Usuario;
import com.example.practica.Enum.EstadoCita;


public interface CitaRepository extends JpaRepository<Cita, Long> {
	List<Cita> findByPaciente_IdAndFecha(long id, LocalDate fecha);
	
	boolean existsByMedico_IdAndFechaAndHora(long id, LocalDate fecha, LocalTime hora);
	
	boolean existsByMedico_IdAndFechaAndHoraAndIdNot(long id, LocalDate fecha, LocalTime hora, long idCita);
	
	long countByPaciente_IdAndFechaAndEstado(long id, LocalDate fecha, EstadoCita est);
	
	List<Cita> findByPaciente_Id(long id);
	
	boolean existsByPacienteAndMedicoAndEstado(Paciente paciente, Medico medico, EstadoCita estado);
	
	// Listar todas las citas con estado PROGRAMADA (para ADMIN)
    List<Cita> findByEstado(EstadoCita estado);

    // Listar citas PROGRAMADAS de un paciente específico (para PACIENTE)
    List<Cita> findByPaciente_UsuarioAndEstado(Usuario usuario, EstadoCita estado);

    List<Cita> findByMedicoAndEstado(Medico medico, EstadoCita estado);

}