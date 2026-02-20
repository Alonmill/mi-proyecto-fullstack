package com.example.practica.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRecetaDTO {

    @NotNull
    private Long medicamentoId;

    @NotBlank
    private String dosis;

    @NotBlank
    private String frecuencia;

    private String medicamentoNombre; 
}