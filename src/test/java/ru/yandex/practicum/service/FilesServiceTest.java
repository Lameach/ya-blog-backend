package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

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
}