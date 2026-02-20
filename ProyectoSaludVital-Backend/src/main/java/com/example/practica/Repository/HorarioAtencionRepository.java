package com.example.practica.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.practica.Model.HorarioAtencion;

public interface HorarioAtencionRepository extends JpaRepository<HorarioAtencion, Long> {
}
