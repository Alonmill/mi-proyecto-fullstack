package com.example.practica.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.practica.DTO.*;
import com.example.practica.Service.MedicoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/medicos")
@RequiredArgsConstructor
public class MedicoController {

    private final MedicoService medicoService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/nuevo")   //  http://localhost:8080/medicos/nuevo
    public ResponseEntity<ObtenerMedicoDTO> agregarMedico(@RequestBody AgregarMedicoDTO request) {
        ObtenerMedicoDTO medicoCreado = medicoService.agregarMedico(request);
        return ResponseEntity.ok(medicoCreado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/editar/{id}") //   http://localhost:8080/medicos/editar/{id}
    public ResponseEntity<ObtenerMedicoDTO> actualizarMedico(@PathVariable Long id, 
                                                              @RequestBody ActualizarMedicoDTO request) {
        ObtenerMedicoDTO medicoActualizado = medicoService.actualizarMedico(id, request);
        return ResponseEntity.ok(medicoActualizado);
    }

    @PreAuthorize("hasRole('MEDICO')")
    @GetMapping("/perfil")  // http://localhost:8080/medicos/perfil
    public ResponseEntity<ObtenerMedicoDTO> verPerfil() {
        ObtenerMedicoDTO perfil = medicoService.verPerfil();
        return ResponseEntity.ok(perfil);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/listado")   //  http://localhost:8080/medicos/listado
    public ResponseEntity<List<ObtenerMedicoDTO>> listarMedicos() {
        return ResponseEntity.ok(medicoService.listarMedicos());
    }
}
