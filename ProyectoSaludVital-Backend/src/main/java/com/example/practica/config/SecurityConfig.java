package com.example.practica.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.practica.security.CustomUserDetailsService;
import com.example.practica.security.JwtFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(""); // 👈 sin prefijo "ROLE_"
    }
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
    	 http
    	 .cors(cors ->{}) // <-- habilita CORS
         .csrf(csrf -> csrf.disable()).csrf(csrf -> csrf.disable())
    	 .authorizeHttpRequests(auth -> auth
    			    // --- PÚBLICO ---
    			    .requestMatchers("/login", "/register", "/access-denied", "/", "/ayuda", "/medicamentos/listado").permitAll()

    			    // --- PACIENTE ---
    			    .requestMatchers("/pacientes/perfil", "/pacientes/expediente").hasRole("PACIENTE")
    			    .requestMatchers("/citas/mis-citas", "/citas/nueva").hasRole("PACIENTE")
    			    .requestMatchers("/recetas/mis-recetas").hasRole("PACIENTE")

    			    // --- MÉDICO ---
    			    .requestMatchers("/medicos/perfil", "/expedientes/entrada/nueva/**").hasRole("MEDICO")
    			    .requestMatchers("/recetas/nueva/**", "/recetas/actualizar").hasRole("MEDICO")

    			    // --- ADMIN ---

    			 
    			    .requestMatchers("/editar/registros/usuarios/**").hasRole("ADMIN")
    			    .requestMatchers( "/pacientes/nuevo").hasRole("ADMIN")
    			    .requestMatchers("/medicos/listado", "/medicos/nuevo", "/medicos/editar/**").hasRole("ADMIN")
    			    .requestMatchers("/medicamentos/editar/**", "/medicamentos/nuevo").hasRole("ADMIN")
    			    .requestMatchers("/admin/dashboard", "/admin/usuarios", "/admin/configuracion").hasRole("ADMIN")
    			    
    			    // --- COMPARTIDOS ---
    			    .requestMatchers("/expedientes/listado").hasAnyRole("MEDICO", "ADMIN")
    			    .requestMatchers("/citas/programadas").hasAnyRole("PACIENTE", "ADMIN")
    			    .requestMatchers("/recetas/listado").hasAnyRole("MEDICO", "ADMIN")
    			    .requestMatchers("/pacientes/listado").hasAnyRole("MEDICO", "ADMIN")
    			    .requestMatchers("/expedientes/editar/**").hasAnyRole("MEDICO", "ADMIN")
    			    .requestMatchers("/expedientes/editar/**").hasAnyRole("MEDICO", "ADMIN")
    			    .requestMatchers("/pacientes/editar/**").hasAnyRole("PACIENTE", "ADMIN")
    			    .requestMatchers("/citas/editar/**").hasAnyRole("PACIENTE", "ADMIN")
    			    .requestMatchers("/citas/cancelar/**").hasAnyRole("PACIENTE", "ADMIN")
    			    .requestMatchers("/expedientes/ver/**").hasAnyRole("MEDICO", "PACIENTE")
    			    .requestMatchers("/citas/listado").hasAnyRole("ADMIN","MEDICO")
    			    .requestMatchers("/recetas/ver/**").hasAnyRole("ADMIN","MEDICO")

    			    // --- NAVEGACIÓN ---
    			    .requestMatchers("/home").authenticated()
    			    
    			    // --- CUALQUIER OTRA ---
    			    .anyRequest().authenticated()
    			)
                .formLogin(form -> form.disable())  // usamos JWT, no login con formulario
                .sessionManagement(session -> session.disable()); // sin sesiones

        // Agregar filtro de JWT
        http.addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
