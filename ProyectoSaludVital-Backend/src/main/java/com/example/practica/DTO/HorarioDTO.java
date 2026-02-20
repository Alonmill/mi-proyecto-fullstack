package com.example.practica.DTO;

import com.example.practica.Enum.DiaSemana;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor

public class HorarioDTO {
    private DiaSemana dia;
    private String horaInicio; // formato "HH:mm"
    private String horaFin;    // formato "HH:mm"
}
