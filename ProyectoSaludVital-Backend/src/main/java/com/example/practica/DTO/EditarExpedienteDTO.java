package com.example.practica.DTO;

import java.time.LocalDate;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditarExpedienteDTO {

	
    private Long id; // ID del expediente que se va a editar
    private String nombrePaciente;
    private String numeroIdentificacion;
    private LocalDate fechaNacimiento;
   
}