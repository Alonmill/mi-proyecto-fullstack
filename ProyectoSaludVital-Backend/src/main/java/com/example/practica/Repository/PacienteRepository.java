package com.example.practica.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.practica.Model.Medico;
import com.example.practica.Model.Paciente;
import com.example.practica.Model.Usuario;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByNumeroIdentificacion(String numeroIdentificacion);
    boolean existsByNumeroIdentificacion(String numeroIdentificacion);
    
    Optional<Paciente> findByUsuario(Usuario usu);
    
    @Query("SELECT DISTINCT c.paciente FROM Cita c WHERE c.medico = :medico")
    List<Paciente> findPacientesByMedico(@Param("medico") Medico medico);
    
    @Query("""
            select distinct c.paciente 
            from Cita c 
            where c.medico.usuario.email = :email 
              and c.estado = 'COMPLETADA'
            """)
     List<Paciente> findPacientesCompletadosPorMedico(@Param("email") String email);
 
}
