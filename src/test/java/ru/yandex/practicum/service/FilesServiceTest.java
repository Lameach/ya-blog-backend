package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import ru.yandex.practicum.exception.BadRequestException;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FilesServiceTest {

    private FilesService filesService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        filesService = new FilesService(tempDir.toString() + "/");
    }

    @Test
    void shouldUploadAndDownloadFileSuccessfully() throws Exception {
        // Подготавливаем фейковый файл
        MockMultipartFile mockFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test data".getBytes()
        );

        String savedFileName = filesService.upload(mockFile);

        assertNotNull(savedFileName);
        assertTrue(savedFileName.endsWith("_test.jpg"));
        assertTrue(Files.exists(tempDir.resolve(savedFileName)), "Файл должен физически появиться на диске");

        Resource downloadedResource = filesService.download(savedFileName);

        assertNotNull(downloadedResource);
        assertEquals("test data", new String(downloadedResource.getContentAsByteArray()));
    }

    @Test
    void shouldDeleteFileSuccessfully() throws Exception {
        Path file = tempDir.resolve("to_delete.txt");
        Files.writeString(file, "dummy content");

        assertTrue(Files.exists(file));

        filesService.deleteFile("to_delete.txt");

        assertFalse(Files.exists(file), "Файл должен быть удален");
    }

    @Test
    void shouldNotWriteOutsideUploadDir() throws Exception {
        Path outside = tempDir.resolve("..").resolve("secret.txt").normalize();
        Files.deleteIfExists(outside);

        MockMultipartFile mockFile = new MockMultipartFile(
                "image",
                "../../secret.txt",
                "text/plain",
                "x".getBytes()
        );

        String savedFileName = filesService.upload(mockFile);

        assertFalse(savedFileName.contains(".."));
        assertTrue(Files.exists(tempDir.resolve(savedFileName)));
        assertFalse(Files.exists(outside));
    }

    @Test
    void shouldNotReadOrDeleteOutsideUploadDir() throws Exception {
        Path outside = tempDir.resolve("..").resolve("secret-outside.txt").normalize();
        Files.writeString(outside, "secret");
        try {
            assertThrows(BadRequestException.class, () -> filesService.download("../secret-outside.txt"));
            assertThrows(BadRequestException.class, () -> filesService.deleteFile("../secret-outside.txt"));
            assertEquals("secret", Files.readString(outside));
        } finally {
            Files.deleteIfExists(outside);
        }
    }
}