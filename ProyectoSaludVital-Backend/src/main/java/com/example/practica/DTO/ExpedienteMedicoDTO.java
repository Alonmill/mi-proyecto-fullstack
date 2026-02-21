package com.example.practica.DTO;

import java.time.LocalDate;
import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpedienteMedicoDTO {

    private Long id;
    private String nombrePaciente;
    private String numeroIdentificacion;
    private LocalDate fechaNacimiento;
    private String nombreMedico;
    private List<EntradaHistorialDTO> entradasHistorial;
}
