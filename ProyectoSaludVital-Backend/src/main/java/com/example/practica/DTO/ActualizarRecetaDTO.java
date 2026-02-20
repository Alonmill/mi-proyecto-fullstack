package com.example.practica.DTO;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarRecetaDTO {

    @NotNull
    private Long idReceta;

    @NotNull
    private List<ItemRecetaDTO> items;
}
