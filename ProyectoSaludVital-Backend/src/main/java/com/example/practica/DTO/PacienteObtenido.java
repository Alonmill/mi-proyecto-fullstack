package com.example.practica.DTO;

import java.time.LocalDate;
import java.util.List;



import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacienteObtenido {

	private long id;
	private String nombre;
	private String numeroIdentificacion;
    private LocalDate fechaNacimiento;
	private List<String> alergias;
	private List<String> enfermedades;
    private Long usuarioId;
}
