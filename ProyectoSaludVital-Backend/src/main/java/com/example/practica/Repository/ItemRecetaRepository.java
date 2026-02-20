package com.example.practica.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.practica.Model.ItemReceta;

public interface ItemRecetaRepository extends JpaRepository<ItemReceta, Long> {
}