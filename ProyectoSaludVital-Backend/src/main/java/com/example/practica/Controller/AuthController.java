package com.example.practica.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.practica.DTO.Login;
import com.example.practica.DTO.ObtenerToken;
import com.example.practica.DTO.Registrar;
import com.example.practica.DTO.UsuarioBusquedaDTO;
import com.example.practica.Model.Role;
import com.example.practica.Model.Usuario;
import com.example.practica.Repository.RoleRepository;
import com.example.practica.Repository.UsuarioRepository;
import com.example.practica.security.JwtUtil;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Registrar request) {
    	
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("El email ya está registrado");
        }

        // Seguridad: el registro público siempre crea PACIENTE,
        // aunque el frontend/envío incluya otro rol.
        Role role = roleRepository.findByName("PACIENTE")
                .orElseThrow(() -> new RuntimeException("Rol PACIENTE no encontrado"));

        Usuario usuario = Usuario.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Collections.singleton(role))
                .build();

        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Usuario registrado exitosamente");
    }

    @PostMapping("/login")
    public ResponseEntity<ObtenerToken> login(@RequestBody Login request) {
        // 1. Autenticar
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Cargar el usuario desde UserDetailsService
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        // 3. Obtener el rol directamente
        String rol = userDetails.getAuthorities()
                .iterator()
                .next()
                .getAuthority(); // 👉 ya te da "ADMIN", "PACIENTE", etc.

        // 4. Generar token con rol
        String token = jwtUtil.generarTokenConRol(userDetails.getUsername(), rol);

        return ResponseEntity.ok(new ObtenerToken(token));
    }
    


    @GetMapping("/admin/usuarios/autocomplete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioBusquedaDTO>> autocompletarUsuariosPorEmail(@RequestParam String query) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.ok(List.of());
        }

        List<UsuarioBusquedaDTO> usuarios = usuarioRepository
                .findTop10ByEmailContainingIgnoreCase(query.trim())
                .stream()
                .map(usuario -> new UsuarioBusquedaDTO(
                        usuario.getId(),
                        usuario.getName(),
                        usuario.getEmail(),
                        usuario.getRoles().stream()
                                .findFirst()
                                .map(Role::getName)
                                .orElse("SIN_ROL")
                ))
                .toList();

        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/admin/usuarios/buscar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioBusquedaDTO> buscarUsuarioPorEmail(@RequestParam String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String rol = usuario.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("SIN_ROL");

        return ResponseEntity.ok(new UsuarioBusquedaDTO(
                usuario.getId(),
                usuario.getName(),
                usuario.getEmail(),
                rol
        ));
    }

        @PutMapping("/editar/registros/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> editarUsuario(@PathVariable Long id, @RequestBody Registrar request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

       
        if(usuarioRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
        	 return ResponseEntity.badRequest().body("El email ya está registrado");
        }
        
        

        // Actualizar campos
        usuario.setName(request.getName());
        usuario.setEmail(request.getEmail());
        
        // Cambiar contraseña solo si viene en el request
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        

        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Usuario actualizado correctamente");
    }
}
