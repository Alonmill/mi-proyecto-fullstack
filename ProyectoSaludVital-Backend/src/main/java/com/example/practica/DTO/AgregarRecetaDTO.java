package com.example.practica.DTO;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgregarRecetaDTO {

    private Long pacienteId;
    @NotNull
    private List<ItemRecetaDTO> items; 
}