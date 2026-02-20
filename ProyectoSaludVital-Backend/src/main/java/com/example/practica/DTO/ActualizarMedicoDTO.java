package com.example.practica.DTO;

import java.math.BigDecimal;
import java.util.List;

import com.example.practica.Enum.Especialidad;
import com.example.practica.Enum.EstadoDoctor;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActualizarMedicoDTO {
	private String nombre;
    private String apellido;
    private String numeroLicencia;
    private String telefono;
    private String email;
    private Especialidad especialidad;
    private EstadoDoctor estado;
    private Boolean disponible;
    private BigDecimal tarifaConsulta;
    private Long usuarioId;  
    private List<HorarioDTO> horarios;
    

}
