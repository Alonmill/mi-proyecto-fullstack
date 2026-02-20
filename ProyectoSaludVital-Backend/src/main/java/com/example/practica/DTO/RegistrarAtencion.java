package com.example.practica.DTO;

import lombok.Data;

@Data
public class RegistrarAtencion {
    private Long citaId;
    private String diagnostico;
    private String tratamiento;
}
