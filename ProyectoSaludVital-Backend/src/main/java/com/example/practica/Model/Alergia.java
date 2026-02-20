package com.example.practica.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alergias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alergia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;
}