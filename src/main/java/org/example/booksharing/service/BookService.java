package org.example.booksharing.service;

import lombok.RequiredArgsConstructor;
import org.example.booksharing.dto.BookDto;
import org.example.booksharing.dto.CreateBookRequest;
import org.example.booksharing.dto.DtoMapper;
import org.example.booksharing.entities.Book;
import org.example.booksharing.entities.User;
import org.example.booksharing.exception.ResourceNotFoundException;
import org.example.booksharing.repository.BookRepository;
import org.example.booksharing.repository.BookSpecification;
import org.example.booksharing.repository.RatingRepository;
import org.example.booksharing.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public List<BookDto> getBooks(String search, Double rating, String tag, String category) {
        List<Book> books;
        if (search != null && !search.isEmpty()) {
            books = bookRepository.search(search);
        } else if (rating != null) {
            books = bookRepository.findByRating(rating);
        } else if (tag != null && !tag.isEmpty()) {
            books = bookRepository.findByTag(tag);
        } else if (category != null && !category.isEmpty()) {
            books = bookRepository.findByCategory(category);
        } else {
            books = bookRepository.findAll();
        }

        String currentUsername = getCurrentUsername();
        return books.stream()
                .map(book -> enrichAndMap(book, currentUsername))
                .toList();
    }

    @Transactional
    public BookDto addBook(CreateBookRequest request, MultipartFile file) throws IOException {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setYear(request.getYear());
        book.setDescription(request.getDescription());
        book.setTags(request.getTags() != null ? request.getTags() : List.of());
        book.setCategory(request.getCategory());
        book.setPublic(request.getIsPublic() != null ? request.getIsPublic() : true);
        book.setUser(user);

        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            String fileUrl = fileService.uploadFile(file, "books");
            if (contentType != null && contentType.startsWith("image/")) {
                book.setImageUrl(fileUrl);
            } else {
                book.setFileUrl(fileUrl);
            }
        }

        Book savedBook = bookRepository.save(book);
        return enrichAndMap(savedBook, username);
    }

    @Transactional
    public BookDto getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        
        book.setActivityScore((book.getActivityScore() != null ? book.getActivityScore() : 0) + 1);
        bookRepository.save(book);
        
        return enrichAndMap(book, getCurrentUsername());
    }

    @Transactional
    public void deleteBook(Long id) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        
        if (!book.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("You cannot delete someone else's book");
        }

        if (book.getFileUrl() != null) fileService.deleteFile(book.getFileUrl());
        if (book.getImageUrl() != null) fileService.deleteFile(book.getImageUrl());

        bookRepository.deleteById(id);
    }

    @Transactional
    public BookDto toggleLike(Long id) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (book.getLikedUsers() == null) {
            book.setLikedUsers(new ArrayList<>());
        }

        boolean isLiked = book.getLikedUsers().stream()
                .anyMatch(u -> u.getId().equals(user.getId()));

        if (isLiked) {
            book.getLikedUsers().removeIf(u -> u.getId().equals(user.getId()));
            book.setLikesCount(Math.max(0, (book.getLikesCount() != null ? book.getLikesCount() : 0L) - 1));
        } else {
            book.getLikedUsers().add(user);
            book.setLikesCount((book.getLikesCount() != null ? book.getLikesCount() : 0L) + 1);
        }

        bookRepository.save(book);
        return enrichAndMap(book, username);
    }

    @Transactional(readOnly = true)
    public List<BookDto> getFavorites() {
        String username = getCurrentUsername();
        List<Book> favorites = bookRepository.findFavoritesByUsername(username);
        return favorites.stream()
                .map(book -> enrichAndMap(book, username))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<BookDto> searchBooks(String search, String author, Integer ratingMin, Integer ratingMax, 
                                     String category, String tag, Integer viewsMin, LocalDateTime updatedAfter, 
                                     Pageable pageable) {
        Page<Book> bookPage = bookRepository.findAll(
                BookSpecification.filter(search, author, ratingMin, ratingMax, category, tag, viewsMin, updatedAfter),
                pageable
        );

        String currentUsername = getCurrentUsername();
        List<BookDto> dtos = bookPage.getContent().stream()
                .map(book -> enrichAndMap(book, currentUsername))
                .toList();
        
        return new PageImpl<>(dtos, pageable, bookPage.getTotalElements());
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return null;
        }
    }

    private BookDto enrichAndMap(Book book, String currentUsername) {
        Double avgRating = ratingRepository.avgScoreByBookId(book.getId());
        BookDto dto = DtoMapper.toBookDto(book);
        dto.setRating(avgRating != null ? avgRating : 0.0);
        
        if (currentUsername != null && book.getLikedUsers() != null) {
            boolean isLiked = book.getLikedUsers().stream()
                    .anyMatch(user -> user.getUsername().equals(currentUsername));
            dto.setIsLikedByUser(isLiked);
        }
        return dto;
    }
}
