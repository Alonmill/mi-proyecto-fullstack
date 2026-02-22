package com.example.practica.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.example.practica.Enum.Especialidad;
import com.example.practica.Enum.EstadoDoctor;

@Entity
@Table(name = "medicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message="El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;
    
    @NotBlank(message="El apellido es obligatorio")
    @Column(nullable = false)
    private String apellido;

    @Column(unique = true, nullable = false)
    private String numeroLicencia;

    
    private String telefono;
    
    @Email
    private String email;

    @Enumerated(EnumType.STRING)
    private Especialidad especialidad;

    @Enumerated(EnumType.STRING)
    private EstadoDoctor estado;

    private boolean disponible;

    private BigDecimal tarifaConsulta;

    private String imagenUrl;

    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    @OneToMany(mappedBy = "medico", cascade = CascadeType.ALL, orphanRemoval = true)

    private List<HorarioAtencion> horarios = new ArrayList<>();
}