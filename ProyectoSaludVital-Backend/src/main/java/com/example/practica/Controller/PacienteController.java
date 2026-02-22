package com.example.practica.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.practica.DTO.*;
import com.example.practica.Service.ImageStorageService;
import com.example.practica.Service.PacienteService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;
    private final ImageStorageService imageStorageService;
    private final ObjectMapper objectMapper;

    @GetMapping("/listado")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<List<PacienteObtenido>> listarPacientes() {
        return ResponseEntity.ok(pacienteService.listarPacientes());
    }

    @PostMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PacienteObtenido> registrarPaciente(@RequestBody AgregarPaciente pacienteDTO) {
        PacienteObtenido creado = pacienteService.agregarPaciente(pacienteDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping(value = "/nuevo", consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PacienteObtenido> registrarPacienteMultipart(
            @RequestPart("data") String data,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) throws Exception {
        AgregarPaciente dto = objectMapper.readValue(data, AgregarPaciente.class);
        if (imagen != null && !imagen.isEmpty()) {
            dto.setImagenUrl(imageStorageService.store(imagen, "pacientes"));
        }
        PacienteObtenido creado = pacienteService.agregarPaciente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('PACIENTE','ADMIN')")
    public ResponseEntity<PacienteObtenido> editarPaciente(@PathVariable Long id, @RequestBody AgregarPaciente pacienteDTO) {
        PacienteObtenido actualizado = pacienteService.editarPaciente(id, pacienteDTO);
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping(value = "/editar/{id}", consumes = { "multipart/form-data" })
    @PreAuthorize("hasAnyRole('PACIENTE','ADMIN')")
    public ResponseEntity<PacienteObtenido> editarPacienteMultipart(@PathVariable Long id,
            @RequestPart("data") String data,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) throws Exception {
        AgregarPaciente dto = objectMapper.readValue(data, AgregarPaciente.class);
        if (imagen != null && !imagen.isEmpty()) {
            dto.setImagenUrl(imageStorageService.store(imagen, "pacientes"));
        }
        return ResponseEntity.ok(pacienteService.editarPaciente(id, dto));
    }

    @GetMapping("/perfil")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<PacienteObtenido> verPerfil() {
        return ResponseEntity.ok(pacienteService.verPerfil());
    }

    @GetMapping("/expediente")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<ExpedienteMedicoDTO> verExpediente(Authentication authentication) {
        return ResponseEntity.ok(pacienteService.verExpediente());
    }
}
