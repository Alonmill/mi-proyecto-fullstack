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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.practica.DTO.*;
import com.example.practica.Service.ImageStorageService;
import com.example.practica.Service.MedicoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/medicos")
@RequiredArgsConstructor
public class MedicoController {

    private final MedicoService medicoService;
    private final ImageStorageService imageStorageService;
    private final ObjectMapper objectMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/nuevo")
    public ResponseEntity<ObtenerMedicoDTO> agregarMedico(@RequestBody AgregarMedicoDTO request) {
        ObtenerMedicoDTO medicoCreado = medicoService.agregarMedico(request);
        return ResponseEntity.ok(medicoCreado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/nuevo", consumes = { "multipart/form-data" })
    public ResponseEntity<ObtenerMedicoDTO> agregarMedicoMultipart(
            @RequestPart("data") String data,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) throws Exception {
        AgregarMedicoDTO request = objectMapper.readValue(data, AgregarMedicoDTO.class);
        if (imagen != null && !imagen.isEmpty()) {
            request.setImagenUrl(imageStorageService.store(imagen, "medicos"));
        }
        return ResponseEntity.ok(medicoService.agregarMedico(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/editar/{id}")
    public ResponseEntity<ObtenerMedicoDTO> actualizarMedico(@PathVariable Long id,
            @RequestBody ActualizarMedicoDTO request) {
        ObtenerMedicoDTO medicoActualizado = medicoService.actualizarMedico(id, request);
        return ResponseEntity.ok(medicoActualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/editar/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<ObtenerMedicoDTO> actualizarMedicoMultipart(@PathVariable Long id,
            @RequestPart("data") String data,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) throws Exception {
        ActualizarMedicoDTO request = objectMapper.readValue(data, ActualizarMedicoDTO.class);
        if (imagen != null && !imagen.isEmpty()) {
            request.setImagenUrl(imageStorageService.store(imagen, "medicos"));
        }
        return ResponseEntity.ok(medicoService.actualizarMedico(id, request));
    }

    @PreAuthorize("hasRole('MEDICO')")
    @GetMapping("/perfil")
    public ResponseEntity<ObtenerMedicoDTO> verPerfil() {
        ObtenerMedicoDTO perfil = medicoService.verPerfil();
        return ResponseEntity.ok(perfil);
    }

    @PreAuthorize("hasRole('MEDICO')")
    @PutMapping("/perfil")
    public ResponseEntity<ObtenerMedicoDTO> actualizarPerfil(@RequestBody ActualizarMedicoDTO request) {
        ObtenerMedicoDTO perfilActualizado = medicoService.actualizarPerfil(request);
        return ResponseEntity.ok(perfilActualizado);
    }

    @PreAuthorize("hasRole('MEDICO')")
    @PutMapping(value = "/perfil", consumes = { "multipart/form-data" })
    public ResponseEntity<ObtenerMedicoDTO> actualizarPerfilMultipart(
            @RequestPart("data") String data,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) throws Exception {
        ActualizarMedicoDTO request = objectMapper.readValue(data, ActualizarMedicoDTO.class);
        if (imagen != null && !imagen.isEmpty()) {
            request.setImagenUrl(imageStorageService.store(imagen, "medicos"));
        }
        return ResponseEntity.ok(medicoService.actualizarPerfil(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PACIENTE')")
    @GetMapping("/listado")
    public ResponseEntity<List<ObtenerMedicoDTO>> listarMedicos() {
        return ResponseEntity.ok(medicoService.listarMedicos());
    }
}
