package com.example.practica.Controller;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
public class ImageController {

    private static final Set<String> ALLOWED = Set.of("medicos", "pacientes", "medicamentos");

    @GetMapping("/uploads/{category}/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String category, @PathVariable String filename) throws MalformedURLException {
        if (!ALLOWED.contains(category)) {
            return ResponseEntity.badRequest().build();
        }

        Path base = Paths.get("uploads").toAbsolutePath().normalize();
        Path file = base.resolve(category).resolve(filename).normalize();

        if (!file.startsWith(base.resolve(category)) || !file.toFile().exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(file.toUri());
        MediaType type = MediaType.IMAGE_JPEG;
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            type = MediaType.IMAGE_PNG;
        }

        return ResponseEntity.ok().contentType(type).body(resource);
    }
}
