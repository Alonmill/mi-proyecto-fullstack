package com.example.practica.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "items_receta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemReceta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dosis;
    private String frecuencia;

    @ManyToOne
    @JoinColumn(name = "receta_id")
    private Receta receta;

    @ManyToOne
    @JoinColumn(name = "medicamento_id")
    private Medicamento medicamento;
}
