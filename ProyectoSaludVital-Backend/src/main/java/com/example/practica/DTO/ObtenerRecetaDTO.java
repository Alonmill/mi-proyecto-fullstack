package com.example.practica.DTO;

import java.time.LocalDate;
import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObtenerRecetaDTO {

    private Long id;
    private LocalDate fechaEmision;
    private LocalDate fechaCaducidad;

    private String medicoNombre;
    private String pacienteNombre;
    private String estado;

    private List<ItemRecetaDTO> items;
    
    	
}
