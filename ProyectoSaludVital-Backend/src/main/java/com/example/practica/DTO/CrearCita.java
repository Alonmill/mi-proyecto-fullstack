package com.example.practica.DTO;

import java.time.LocalDate;
import java.time.LocalTime;


import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearCita {
	
	@NotNull
	private long idPaciente;
	
	@NotNull
	private long idMedico;
	
	@NotNull
	@FutureOrPresent
	private LocalDate fecha;
	@NotNull
	private LocalTime hora;
	@NotBlank
	private String motivo;
}
