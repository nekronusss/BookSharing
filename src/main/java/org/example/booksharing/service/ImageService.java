package org.example.booksharing.service;

import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class ImageService {
    private final Path uploadDir = Paths.get("uploads/books");

    public List<String> uploadImages(Long bookId, MultipartFile[] files) throws IOException {
        Files.createDirectories(uploadDir);
        List<String> paths = new ArrayList<>();
        for (MultipartFile f : files) {
            String name = UUID.randomUUID() + "_" + f.getOriginalFilename();
            Path dest = uploadDir.resolve(name);
            f.transferTo(dest.toFile());

            BufferedImage original = ImageIO.read(dest.toFile());
            BufferedImage thumb = Scalr.resize(original, 200);
            ImageIO.write(thumb, "jpg", uploadDir.resolve("thumb_" + name).toFile());

            paths.add("/uploads/books/" + name);
        }
        return paths;
    }
}
