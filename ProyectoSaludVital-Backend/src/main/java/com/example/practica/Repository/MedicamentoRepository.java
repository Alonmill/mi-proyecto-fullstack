package com.example.practica.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.practica.Model.Medicamento;

import java.util.Optional;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
    Optional<Medicamento> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}