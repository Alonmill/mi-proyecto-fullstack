package com.example.practica.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RestController;

import com.example.practica.Service.RecetaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.example.practica.DTO.*;

@RestController
@RequestMapping("/recetas")
@RequiredArgsConstructor
public class RecetaController {

    private final RecetaService recetaService;


    @GetMapping("/listado")  //   http://localhost:8080/recetas/listado
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')") 
    public ResponseEntity<List<ObtenerRecetaDTO>> listarRecetas() {
        return ResponseEntity.ok(recetaService.listarRecetas());
    }

    @GetMapping("/mis-recetas")   //    http://localhost:8080/recetas/mis-recetas
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<List<ObtenerRecetaDTO>> verMisRecetas() {
        return ResponseEntity.ok(recetaService.verMisRecetas());
    }
    
    @PutMapping("/actualizar")  //    http://localhost:8080/recetas/actualizar
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<ObtenerRecetaDTO> actualizarReceta(
            @Valid @RequestBody ActualizarRecetaDTO dto) {

        ObtenerRecetaDTO recetaActualizada = recetaService.actualizarReceta(dto);
        return ResponseEntity.ok(recetaActualizada);
    } 


    @PostMapping("/nueva/{idPaciente}")  //  http://localhost:8080/recetas/nueva/{idPaciente}
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<ObtenerRecetaDTO> emitirReceta( @PathVariable Long idPaciente,@Valid @RequestBody AgregarRecetaDTO dto) {

        // Reemplazamos pacienteId en el DTO por el pathVariable
        dto.setPacienteId(idPaciente);

        ObtenerRecetaDTO receta = recetaService.emitirReceta(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(receta);
    }
    

    @GetMapping("/ver/{id}")   //  http://localhost:8080/recetas/ver/{id}
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")   
    public ResponseEntity<ObtenerRecetaDTO> verReceta(@PathVariable Long id) {
        return ResponseEntity.ok(recetaService.verReceta(id));
    }
}
