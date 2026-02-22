package com.example.practica.DTO;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AgregarPaciente {

	@NotBlank
	private String nombre;
	
	@NotBlank
	private String numeroIdentificacion;
	
	@NotNull
	private LocalDate fechaNacimiento;
	
	@NotNull
	@NotEmpty
	private List<String> alergias;
	
	@NotNull
	@NotEmpty
	private List<String> enfermedades;
	
	private Long usuarioId;
	private String imagenUrl;
	
}
