package com.example.practica.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.practica.DTO.ActualizarRecetaDTO;
import com.example.practica.DTO.AgregarRecetaDTO;
import com.example.practica.DTO.ObtenerRecetaDTO;
import com.example.practica.Service.RecetaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/recetas")
@RequiredArgsConstructor
public class RecetaController {

    private final RecetaService recetaService;

    @GetMapping("/listado")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<List<ObtenerRecetaDTO>> listarRecetas() {
        return ResponseEntity.ok(recetaService.listarRecetas());
    }

    @GetMapping("/mis-recetas")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<List<ObtenerRecetaDTO>> verMisRecetas() {
        return ResponseEntity.ok(recetaService.verMisRecetas());
    }

    @PutMapping("/actualizar")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<ObtenerRecetaDTO> actualizarReceta(@Valid @RequestBody ActualizarRecetaDTO dto) {
        return ResponseEntity.ok(recetaService.actualizarReceta(dto));
    }

    @PutMapping("/{id}/anular")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<ObtenerRecetaDTO> anularReceta(@PathVariable Long id) {
        return ResponseEntity.ok(recetaService.anularReceta(id));
    }

    @PostMapping("/{id}/duplicar")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<ObtenerRecetaDTO> duplicarReceta(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recetaService.duplicarReceta(id));
    }

    @PostMapping("/{id}/renovar")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<ObtenerRecetaDTO> renovarReceta(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recetaService.renovarReceta(id));
    }

    @PostMapping("/nueva/{idPaciente}")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<ObtenerRecetaDTO> emitirReceta(@PathVariable Long idPaciente, @Valid @RequestBody AgregarRecetaDTO dto) {
        dto.setPacienteId(idPaciente);
        return ResponseEntity.status(HttpStatus.CREATED).body(recetaService.emitirReceta(dto));
    }

    @GetMapping("/ver/{id}")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<ObtenerRecetaDTO> verReceta(@PathVariable Long id) {
        return ResponseEntity.ok(recetaService.verReceta(id));
    }
}
