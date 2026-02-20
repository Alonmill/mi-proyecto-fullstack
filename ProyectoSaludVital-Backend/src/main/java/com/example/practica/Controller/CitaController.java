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

import com.example.practica.DTO.ActualizarCita;
import com.example.practica.DTO.CitaObtenida;
import com.example.practica.DTO.CrearCita;
import com.example.practica.DTO.ListaCitaPaciente;
import com.example.practica.Service.CitaService;
import com.example.practica.Repository.CitaRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/citas")
@RequiredArgsConstructor
public class CitaController {
	private final CitaService citaservice;
	private final CitaRepository citarepo;
	
	@PostMapping("/nueva")     //   http://localhost:8080/citas/nueva
	@PreAuthorize("hasRole('PACIENTE')")
	public ResponseEntity<CitaObtenida> SolicitarCita(@Valid @RequestBody CrearCita citaDTO) {
	    CitaObtenida nueva = citaservice.crearCita(citaDTO);
	    return ResponseEntity.ok(nueva);
	}
	
	@PutMapping("editar/{id}")
	@PreAuthorize("hasAnyRole('PACIENTE','ADMIN')")
	public ResponseEntity<CitaObtenida> actualizarCita(@PathVariable("id") long id, @Valid @RequestBody ActualizarCita actualizar){
		CitaObtenida cita = citaservice.actualizarCita(id, actualizar);
		return ResponseEntity.ok(cita);
	}
	
	// Endpoint para listar citas PROGRAMADAS según rol
    @GetMapping("/programadas")
    @PreAuthorize("hasAnyRole('PACIENTE','ADMIN')")
    public ResponseEntity<List<CitaObtenida>> listarCitasProgramadas() {
        List<CitaObtenida> citas = citaservice.listarCitasProgramadas();
        return ResponseEntity.ok(citas);
    }
    
	
	@PutMapping("/cancelar/{id}")
	@PreAuthorize("hasAnyRole('PACIENTE','ADMIN')")
	public ResponseEntity<CitaObtenida> cancelarCita(@PathVariable("id") long id) {
	    CitaObtenida citaCancelada = citaservice.cancelarCita(id);
	    return ResponseEntity.ok(citaCancelada);
	}

	
	@GetMapping("/mis-citas")  //  http://localhost:8080/citas/mis-citas
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<List<ListaCitaPaciente>> listarMisCitas() {
        List<ListaCitaPaciente> citas = citaservice.obtenerCitasPacienteActual();
        return ResponseEntity.ok(citas);
    }
	
	@GetMapping("/listado")
	@PreAuthorize("hasAnyRole('ADMIN','MEDICO')")
	public ResponseEntity<List<ListaCitaPaciente>> listadoDeCitas() {
	    List<ListaCitaPaciente> citas = citarepo.findAll()
	        .stream()
	        .map(c -> {
	            ListaCitaPaciente dto = new ListaCitaPaciente();
	            dto.setId(c.getId());
	            dto.setFecha(c.getFecha());
	            dto.setHora(c.getHora());
	            dto.setMotivo(c.getMotivo());
	            dto.setPacienteNombre(c.getPaciente().getNombre());
	            dto.setMedicoNombre(c.getMedico().getNombre());
	            dto.setTarifa(c.getTarifaAplicada());
	            dto.setEstado(c.getEstado().name());
	            return dto;
	        })
	        .toList();
	    return ResponseEntity.ok(citas);
	}
}
