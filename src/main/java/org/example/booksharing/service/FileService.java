package org.example.booksharing.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileService {
    private static final Path ROOT = Path.of("uploads").toAbsolutePath().normalize();
    private static final long MAX_SIZE = 10 * 1024 * 1024;

    public String uploadFile(MultipartFile file, String subDir) throws IOException {
        if (file == null || file.isEmpty()) return null;
        if (!java.util.Set.of("books", "avatars").contains(subDir))
            throw new IllegalArgumentException("Invalid upload destination");
        if (file.getSize() > MAX_SIZE) throw new IllegalArgumentException("Maximum image size is 10 MB");
        BufferedImage decoded;
        try (var input = ImageIO.createImageInputStream(file.getInputStream())) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IllegalArgumentException("Upload a valid JPEG or PNG image");
            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName();
                if (!format.equalsIgnoreCase("JPEG") && !format.equalsIgnoreCase("PNG"))
                    throw new IllegalArgumentException("Only JPEG and PNG images are supported");
                reader.setInput(input);
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels > 16_000_000) throw new IllegalArgumentException("Image resolution must not exceed 16 megapixels");
                decoded = reader.read(0);
            } finally { reader.dispose(); }
        }
        if (decoded == null) throw new IllegalArgumentException("Invalid image");
        Path directory = ROOT.resolve(subDir);
        Files.createDirectories(directory);
        String name = UUID.randomUUID() + ".png";
        // Re-encode the decoded image; never trust the original filename or MIME header.
        ImageIO.write(decoded, "png", directory.resolve(name).toFile());
        return "/uploads/" + subDir + "/" + name;
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) return;
        Path target = ROOT.resolve(fileUrl.substring("/uploads/".length())).normalize();
        if (!target.startsWith(ROOT) || target.equals(ROOT)) return;
        try { Files.deleteIfExists(target); }
        catch (IOException ex) {
            org.slf4j.LoggerFactory.getLogger(FileService.class).warn("Unable to delete unused upload", ex);
        }
    }
}
