package com.example.practica.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.practica.Model.Alergia;

public interface AlergiaRepository extends JpaRepository<Alergia, Long> {
	
}
