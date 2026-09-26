package org.example.booksharing.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.List;

@Service
public class FileService {

    private static final String UPLOAD_DIR = "uploads";
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/jpg");
    private static final List<String> ALLOWED_FILE_TYPES = List.of("application/pdf");

    public String uploadFile(MultipartFile file, String subDir) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File too large (max 50MB)");
        }

        String contentType = file.getContentType();
        boolean isImage = contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType);
        boolean isPdf = contentType != null && ALLOWED_FILE_TYPES.contains(contentType);

        if (!isImage && !isPdf) {
            throw new IllegalArgumentException("Invalid file type. Allowed: JPEG, PNG, PDF");
        }

        Path uploadPath = Paths.get(UPLOAD_DIR, subDir);
        Files.createDirectories(uploadPath);

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        return "/" + UPLOAD_DIR + "/" + subDir + "/" + fileName;
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty() || !fileUrl.startsWith("/")) {
            return;
        }

        try {
            Path filePath = Paths.get(System.getProperty("user.dir"), fileUrl.substring(1));
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error
        }
    }
}
