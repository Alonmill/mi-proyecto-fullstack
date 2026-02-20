package com.example.practica.Service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.practica.Model.Medicamento;

import com.example.practica.Model.Usuario;
import com.example.practica.Repository.MedicamentoRepository;
import com.example.practica.Repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MedicamentoService {

	private final MedicamentoRepository medicamenRepo;
	private final UsuarioRepository usuarioRepo;
	
	@Transactional
	public Medicamento actualizar(long id, Medicamento datosMedicamento) {
	   
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    String email = auth.getName();

	    Usuario usuarioActual = usuarioRepo.findByEmail(email)
	            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

	    if (!usuarioActual.tieneRol("ADMIN")) {
	        throw new RuntimeException("No tienes permisos para actualizar medicamentos");
	    }

	    
	    Medicamento medicamento = medicamenRepo.findById(id)
	            .orElseThrow(() -> new RuntimeException("Medicamento no encontrado"));

	   
	    medicamento.setNombre(datosMedicamento.getNombre());
	    medicamento.setDescripcion(datosMedicamento.getDescripcion());

	    return medicamenRepo.save(medicamento);
	}

	
}
