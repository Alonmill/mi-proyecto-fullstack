package com.example.practica.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CitaObtenida {
	private Long id;
	private LocalDate fecha; 
	private LocalTime hora;
	private String motivo; 
	private String pacienteNombre;
	private String medicoNombre; 
	private Long medicoId;
}