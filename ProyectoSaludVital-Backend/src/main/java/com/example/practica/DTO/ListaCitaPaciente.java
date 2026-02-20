package com.example.practica.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.example.practica.Enum.EstadoCita;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ListaCitaPaciente {
    private Long id;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private String pacienteNombre;
    private String medicoNombre;
    private BigDecimal tarifa;

    private String estado;
    
}
