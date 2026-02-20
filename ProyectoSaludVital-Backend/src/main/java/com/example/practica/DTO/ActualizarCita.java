package com.example.practica.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class ActualizarCita {
	
	@NotNull
	private long idMedico;
	
	@NotNull
	@Future
	private LocalDate fecha;
	
	@NotNull
	private LocalTime hora;
	
	@NotBlank
	private String motivo;

}
