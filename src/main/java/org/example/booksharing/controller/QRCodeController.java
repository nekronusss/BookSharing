package org.example.booksharing.controller;

import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import org.example.booksharing.dto.BookDto;
import org.example.booksharing.dto.DtoMapper;
import org.example.booksharing.entities.Book;
import org.example.booksharing.exception.ResourceNotFoundException;
import org.example.booksharing.repository.BookRepository;
import org.example.booksharing.repository.RatingRepository;
import org.example.booksharing.service.QRCodeService;
import org.example.booksharing.util.SecurityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/qrcode")
@RequiredArgsConstructor
public class QRCodeController {

    private final QRCodeService qrCodeService;
    private final BookRepository bookRepository;
    private final RatingRepository ratingRepository;

    @GetMapping("/book/{bookId}")
    public ResponseEntity<byte[]> generateQRCodeByBookId(@PathVariable Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (book.getQrCode() == null || book.getQrCode().isEmpty()) {
            throw new IllegalArgumentException("Book does not have a QR code");
        }

        try {
            byte[] qrCodeImage = qrCodeService.generateQRCodeImage(book.getQrCode());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrCodeImage.length);
            return new ResponseEntity<>(qrCodeImage, headers, HttpStatus.OK);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    @GetMapping("/{qrCode}")
    public ResponseEntity<byte[]> generateQRCode(@PathVariable String qrCode) {
        try {
            byte[] qrCodeImage = qrCodeService.generateQRCodeImage(qrCode);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrCodeImage.length);
            return new ResponseEntity<>(qrCodeImage, headers, HttpStatus.OK);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    @GetMapping("/scan/{qrCode}")
    public ResponseEntity<?> scanQRCode(@PathVariable String qrCode) {
        Optional<Book> bookOpt = bookRepository.findByQrCode(qrCode);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            BookDto bookDto = DtoMapper.toBookDto(book);
            
            Double avgRating = ratingRepository.avgScoreByBookId(book.getId());
            bookDto.setRating(avgRating != null ? avgRating : 0.0);
            
            String currentUsername = SecurityUtils.getCurrentUsername();
            if (currentUsername != null && book.getLikedUsers() != null) {
                boolean isLiked = book.getLikedUsers().stream()
                        .anyMatch(user -> user.getUsername().equals(currentUsername));
                bookDto.setIsLikedByUser(isLiked);
            }
            
            return ResponseEntity.ok(bookDto);
        }
        
        throw new ResourceNotFoundException("QR code not found");
    }
}

