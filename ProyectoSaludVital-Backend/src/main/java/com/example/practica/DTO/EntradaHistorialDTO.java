package com.example.practica.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.practica.Enum.EstadoCita;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntradaHistorialDTO {
    private Long id;
    private LocalDate fecha;
    private LocalTime hora;
    private String diagnostico;
    private String tratamiento;
    private String nombreMedico;
    private String nombrePaciente;
    private EstadoCita estadoCita;
}
