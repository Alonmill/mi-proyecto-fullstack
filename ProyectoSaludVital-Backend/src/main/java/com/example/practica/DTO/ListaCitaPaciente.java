package com.example.practica.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ListaCitaPaciente {
    private Long id;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private Long pacienteId;
    private String pacienteNombre;
    private String medicoNombre;
    private BigDecimal tarifa;
    private String estado;
}
