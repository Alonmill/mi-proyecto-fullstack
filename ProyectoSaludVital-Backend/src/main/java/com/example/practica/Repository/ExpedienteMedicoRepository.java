package com.example.practica.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.practica.Model.ExpedienteMedico;
import com.example.practica.Model.Paciente;

public interface ExpedienteMedicoRepository extends JpaRepository<ExpedienteMedico,Long> {
	Optional<ExpedienteMedico> findByPaciente(Paciente paciente);
	@Query("SELECT DISTINCT e FROM ExpedienteMedico e JOIN e.entradas en WHERE en.medico.usuario.email = :email")
	List<ExpedienteMedico> findExpedientesPorMedico(@Param("email") String email);




}
