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

import com.example.practica.DTO.RegistrarAtencion;
import com.example.practica.Service.AtencionService;
import com.example.practica.Service.ExpedienteService;

import com.example.practica.DTO.EntradaHistorialDTO;
import com.example.practica.DTO.ExpedienteMedicoDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/expedientes")
@RequiredArgsConstructor
public class ExpedienteController {
	
	private final AtencionService atencionService;
	private final ExpedienteService expedienteService;
	
	// Listado de todos los expedientes (ADMIN y MEDICO)
    @GetMapping("/listado")                   //    http://localhost:8080/expedientes/listado
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')") 
    public List<ExpedienteMedicoDTO> listarExpedientes() {
        return expedienteService.listarExpedientes();
    }

    // Ver un expediente por ID (PACIENTE y MEDICO)
    @GetMapping("/ver/{id}")
    @PreAuthorize("hasAnyRole('PACIENTE','MEDICO')")
    public ExpedienteMedicoDTO verExpediente(@PathVariable Long id) {
        return expedienteService.verExpediente(id);
    }

    // Editar un expediente (ADMIN y MEDICO)
    @PutMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDICO')")
    public ExpedienteMedicoDTO editarExpediente(
            @PathVariable Long id,
            @RequestBody RegistrarAtencion request) {
        return expedienteService.editarExpediente(id, request);
    }

	@PostMapping("/entrada/nueva/{idPaciente}")
	@PreAuthorize("hasRole('MEDICO')")
	public ResponseEntity<EntradaHistorialDTO> nuevaEntrada(@PathVariable Long idPaciente,@RequestBody RegistrarAtencion request) {
	    return ResponseEntity.ok(atencionService.registrarAtencion(idPaciente, request));
	}
	
	
}
