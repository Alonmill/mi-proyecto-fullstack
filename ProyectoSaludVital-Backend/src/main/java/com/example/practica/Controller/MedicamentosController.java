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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.practica.Repository.MedicamentoRepository;
import com.example.practica.Service.ImageStorageService;
import com.example.practica.Service.MedicamentoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;

import com.example.practica.Model.Medicamento;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/medicamentos")
@RequiredArgsConstructor
public class MedicamentosController {

	private final MedicamentoRepository medicamenrepo;
	private final MedicamentoService medicaservice;
	private final ImageStorageService imageStorageService;
	private final ObjectMapper objectMapper;
	
	@GetMapping("/listado")
	public ResponseEntity<List<Medicamento>> listadoMedicamentos(){
		List<Medicamento> listadoMedicamen = medicamenrepo.findAll();
		return ResponseEntity.ok(listadoMedicamen);
	}
	
	@PostMapping("/nuevo")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Medicamento> agregarMedicamento(@Valid @RequestBody Medicamento medica){
		Medicamento medicamen = medicamenrepo.save(medica);
		return ResponseEntity.status(HttpStatus.CREATED).body(medicamen);
	}

	@PostMapping(value = "/nuevo", consumes = { "multipart/form-data" })
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Medicamento> agregarMedicamentoMultipart(
			@RequestPart("data") String data,
			@RequestPart(value = "imagen", required = false) MultipartFile imagen) throws Exception {
		Medicamento medica = objectMapper.readValue(data, Medicamento.class);
		if (imagen != null && !imagen.isEmpty()) {
			medica.setImagenUrl(imageStorageService.store(imagen, "medicamentos"));
		}
		Medicamento medicamen = medicamenrepo.save(medica);
		return ResponseEntity.status(HttpStatus.CREATED).body(medicamen);
	}
	
	@PutMapping("/editar/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Medicamento> actualizarMedicamento(@PathVariable("id") long id, @Valid @RequestBody Medicamento medica){
		try {
			Medicamento medicamen = medicaservice.actualizar(id, medica);
			return ResponseEntity.ok(medicamen);
		}catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PutMapping(value = "/editar/{id}", consumes = { "multipart/form-data" })
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Medicamento> actualizarMedicamentoMultipart(@PathVariable("id") long id,
			@RequestPart("data") String data,
			@RequestPart(value = "imagen", required = false) MultipartFile imagen){
		try {
			Medicamento medica = objectMapper.readValue(data, Medicamento.class);
			if (imagen != null && !imagen.isEmpty()) {
				medica.setImagenUrl(imageStorageService.store(imagen, "medicamentos"));
			}
			Medicamento medicamen = medicaservice.actualizar(id, medica);
			return ResponseEntity.ok(medicamen);
		}catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}
}
