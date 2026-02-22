package com.example.practica.DTO;

import java.math.BigDecimal;
import java.util.List;

import com.example.practica.Enum.Especialidad;
import com.example.practica.Enum.EstadoDoctor;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObtenerMedicoDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String numeroLicencia;
    private String telefono;
    private String email;
    private Especialidad especialidad;
    private EstadoDoctor estado;
    private boolean disponible;
    private BigDecimal tarifaConsulta;
    private long usuarioId;
    private String imagenUrl;
    private List<HorarioDTO> horarios;
}