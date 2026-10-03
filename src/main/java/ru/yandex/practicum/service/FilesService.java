package ru.yandex.practicum.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FilesService {

    public final String uploadDir;
    
    public FilesService(@Value("${upload.dir:uploads/}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String upload(MultipartFile file) {
        try {
            Path uploadDir = Paths.get(this.uploadDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalName = file.getOriginalFilename();
            String uniqueFileName = java.util.UUID.randomUUID() + "_" + originalName;
            Path filePath = uploadDir.resolve(uniqueFileName);
            file.transferTo(filePath);

            return uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Resource download(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new RuntimeException("Отсутствует имя файла");
        }
        try {
            Path filePath = Paths.get(this.uploadDir).resolve(filename).normalize();
            byte[] content = Files.readAllBytes(filePath);

            return new ByteArrayResource(content);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void deleteFile(String filename) {
        if (filename == null || filename.isBlank()) return;

        try {
            Path filePath = Paths.get(this.uploadDir).resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при удалении файла: " + e.getMessage(), e);
        }
    }

}