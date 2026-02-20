package com.example.practica.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.practica.Model.Medico;

import com.example.practica.Model.Usuario;

import java.util.Optional;

public interface MedicoRepository extends JpaRepository<Medico, Long> {
    Optional<Medico> findByNumeroLicencia(String numeroLicencia);
    boolean existsByNumeroLicencia(String numeroLicencia);
    
    Optional<Medico> findByUsuario(Usuario usu);
    
    Optional<Medico> findByUsuarioId(long id);
}