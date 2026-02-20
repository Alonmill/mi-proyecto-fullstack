package com.example.practica.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import com.example.practica.DTO.*;

import com.example.practica.Service.PacienteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;



    @GetMapping("/listado")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")     //   http://localhost:8080/pacientes/listado
    public ResponseEntity<List<PacienteObtenido>> listarPacientes() {
        return ResponseEntity.ok(pacienteService.listarPacientes());
    }


    @PostMapping("/nuevo") //  http://localhost:8080/pacientes/nuevo
    @PreAuthorize("hasRole('ADMIN')")  
    public ResponseEntity<PacienteObtenido> registrarPaciente(@RequestBody AgregarPaciente pacienteDTO) {
        PacienteObtenido creado = pacienteService.agregarPaciente(pacienteDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/editar/{id}")  //  http://localhost:8080/pacientes/editar/{id}
    @PreAuthorize("hasAnyRole('PACIENTE','ADMIN')")  
    public ResponseEntity<PacienteObtenido> editarPaciente(
            @PathVariable Long id,
            @RequestBody AgregarPaciente pacienteDTO) {
        PacienteObtenido actualizado = pacienteService.editarPaciente(id, pacienteDTO);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/perfil")
    @PreAuthorize("hasRole('PACIENTE')")  //   http://localhost:8080/pacientes/perfil
    public ResponseEntity<PacienteObtenido> verPerfil() {
        return ResponseEntity.ok(pacienteService.verPerfil());
    }


    @GetMapping("/expediente")
    @PreAuthorize("hasRole('PACIENTE')")   //  http://localhost:8080/pacientes/expediente
    public ResponseEntity<ExpedienteMedicoDTO> verExpediente(Authentication authentication) {
    	return ResponseEntity.ok(pacienteService.verExpediente());
    }
}
