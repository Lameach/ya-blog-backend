package ru.yandex.practicum.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.exception.BadRequestException;
import ru.yandex.practicum.exception.NotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FilesService {

    public final String uploadDir;

    public FilesService(@Value("${upload.dir:uploads/}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Файл не передан");
        }
        try {
            Path root = uploadRoot();
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            String uniqueFileName = UUID.randomUUID() + "_" + sanitizeOriginalName(file.getOriginalFilename());
            Path filePath = resolveInsideUploadDir(uniqueFileName);
            file.transferTo(filePath);

            return uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Resource download(String filename) {
        Path filePath = resolveInsideUploadDir(filename);
        try {
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                throw new NotFoundException("Файл не найден");
            }
            byte[] content = Files.readAllBytes(filePath);
            return new ByteArrayResource(content);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void deleteFile(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        Path filePath = resolveInsideUploadDir(filename);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при удалении файла: " + e.getMessage(), e);
        }
    }

    private String sanitizeOriginalName(String original) {
        String name = "file";
        if (original != null && !original.isBlank()) {
            name = Paths.get(original).getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
        }
        if (name.isBlank() || name.equals(".") || name.equals("..")) {
            return "file";
        }
        return name;
    }

    private Path uploadRoot() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private Path resolveInsideUploadDir(String filename) {
        if (filename == null || filename.isBlank()
                || filename.contains("..")
                || filename.contains("/")
                || filename.contains("\\")) {
            throw new BadRequestException("Некорректное имя файла");
        }
        Path root = uploadRoot();
        Path filePath = root.resolve(filename).normalize();
        if (!filePath.startsWith(root)) {
            throw new BadRequestException("Некорректное имя файла");
        }
        return filePath;
    }
}
