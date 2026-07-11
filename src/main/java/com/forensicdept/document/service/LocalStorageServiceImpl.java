package com.forensicdept.document.service;

import com.forensicdept.config.AppProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;

/**
 * Local filesystem implementation of {@link StorageService}.
 * Files are stored under: {@code <documentBasePath>/<subDirectory>/<fileName>}
 *
 * <p>To swap to S3: create an {@code S3StorageServiceImpl} and make it {@code @Primary}.
 * No other code changes needed.
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class LocalStorageServiceImpl implements StorageService {

    private final AppProperties appProperties;

    @PostConstruct
    public void init() throws IOException {
        Path base = Path.of(appProperties.getStorage().getDocumentBasePath());
        Files.createDirectories(base);
        log.info("Document storage initialized at: {}", base.toAbsolutePath());
    }

    @Override
    public String store(InputStream inputStream, String fileName, String subDirectory) {
        try {
            Path dir = Path.of(appProperties.getStorage().getDocumentBasePath(), subDirectory);
            Files.createDirectories(dir);
            Path target = dir.resolve(fileName);
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            String storagePath = subDirectory + "/" + fileName;
            log.info("Stored file: {}", storagePath);
            return storagePath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + fileName, e);
        }
    }

    @Override
    public InputStream retrieve(String storagePath) {
        try {
            Path path = Path.of(appProperties.getStorage().getDocumentBasePath(), storagePath);
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new RuntimeException("File not found: " + storagePath, e);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Path path = Path.of(appProperties.getStorage().getDocumentBasePath(), storagePath);
            Files.deleteIfExists(path);
            log.info("Deleted file: {}", storagePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + storagePath, e);
        }
    }
}
