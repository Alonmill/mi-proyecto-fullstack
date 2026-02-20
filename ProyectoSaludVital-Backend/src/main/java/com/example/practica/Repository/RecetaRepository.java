package com.example.practica.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.practica.Model.Paciente;
import com.example.practica.Model.Receta;

public interface RecetaRepository extends JpaRepository<Receta, Long> {
	List<Receta> findByPaciente(Paciente paciente);
	List<Receta> findByMedicoUsuarioEmail(String email);

}
