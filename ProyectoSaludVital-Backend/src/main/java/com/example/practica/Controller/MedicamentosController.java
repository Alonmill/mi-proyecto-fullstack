package com.example.practica.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.practica.Repository.MedicamentoRepository;
import com.example.practica.Service.MedicamentoService;

import jakarta.validation.Valid;

import com.example.practica.Model.Medicamento;
import org.springframework.web.bind.annotation.PathVariable;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/medicamentos")
@RequiredArgsConstructor
public class MedicamentosController {

	private final MedicamentoRepository medicamenrepo;
	private final MedicamentoService medicaservice;
	
	
	@GetMapping("/listado")  //   http://localhost:8080/medicamentos/listado
	public ResponseEntity<List<Medicamento>> listadoMedicamentos(){
		List<Medicamento> listadoMedicamen = medicamenrepo.findAll();
		return ResponseEntity.ok(listadoMedicamen);
	}
	
	@PostMapping("/nuevo")  //   http://localhost:8080/medicamentos/nuevo
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Medicamento> agregarMedicamento(@Valid @RequestBody Medicamento medica){
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
}
