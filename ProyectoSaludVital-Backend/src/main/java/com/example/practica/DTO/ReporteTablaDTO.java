package com.example.practica.DTO;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReporteTablaDTO {
    private String titulo;
    private LocalDateTime fechaGeneracion;
    private List<String> headers;
    private List<List<String>> rows;
}
