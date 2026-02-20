package com.example.practica.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.practica.Model.Enfermedad;

public interface EnfermedadRepository extends JpaRepository<Enfermedad, Long> {
}