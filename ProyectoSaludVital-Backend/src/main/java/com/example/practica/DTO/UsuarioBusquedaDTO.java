package com.example.practica.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioBusquedaDTO {
    private Long id;
    private String name;
    private String email;
    private String rol;
}
