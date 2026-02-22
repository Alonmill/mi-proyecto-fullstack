package com.example.practica.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorageService {

    private static final long MAX_SIZE = 2 * 1024 * 1024; // 2MB
    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/jpg");
    private final Path uploadRoot = Paths.get("uploads").toAbsolutePath().normalize();

    public String store(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (!ALLOWED.contains(file.getContentType())) {
            throw new RuntimeException("Tipo de archivo no permitido. Solo jpg, jpeg, png");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new RuntimeException("El archivo supera el tamaño máximo de 2MB");
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "img" : file.getOriginalFilename());
        String ext = "";
        int idx = original.lastIndexOf('.');
        if (idx > -1) {
            ext = original.substring(idx).toLowerCase();
        }

        String filename = UUID.randomUUID() + ext;

        try {
            Path categoryDir = uploadRoot.resolve(category).normalize();
            Files.createDirectories(categoryDir);

            Path target = categoryDir.resolve(filename).normalize();
            if (!target.startsWith(categoryDir)) {
                throw new RuntimeException("Ruta de archivo inválida");
            }

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "uploads/" + category + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar la imagen", e);
        }
    }
}
