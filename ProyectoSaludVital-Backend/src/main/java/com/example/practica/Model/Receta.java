package com.example.practica.Model;

import com.example.practica.Enum.EstadoReceta;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "recetas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaEmision;
    private LocalDate fechaCaducidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReceta estado;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemReceta> items;
}
