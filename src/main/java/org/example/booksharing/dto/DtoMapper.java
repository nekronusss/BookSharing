package org.example.booksharing.dto;

import org.example.booksharing.entities.Book;
import org.example.booksharing.entities.Comment;
import org.example.booksharing.entities.Rating;
import org.example.booksharing.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class DtoMapper {

    public static UserDto toUserDto(User user) {
        if (user == null) return null;
        
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setPrivateProfile(user.isPrivateProfile());
        dto.setPrivateBooks(user.isPrivateBooks());
        return dto;
    }

    public static BookDto toBookDto(Book book) {
        if (book == null) return null;
        
        BookDto dto = new BookDto();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setYear(book.getYear());
        dto.setDescription(book.getDescription());
        dto.setAddedDate(book.getAddedDate());
        dto.setQrCode(book.getQrCode());
        dto.setRating(book.getRating() != null ? book.getRating() : 0.0);
        dto.setLikesCount(book.getLikesCount() != null ? book.getLikesCount() : 0L);
        dto.setViews(book.getViews() != null ? book.getViews() : 0);
        dto.setUpdatedAt(book.getUpdatedAt());
        dto.setTags(book.getTags() != null ? book.getTags() : List.of());
        dto.setFileUrl(book.getFileUrl() != null ? book.getFileUrl() : "");
        dto.setImageUrl(book.getImageUrl() != null ? book.getImageUrl() : "");
        dto.setIsPublic(book.getPublic() != null ? book.getPublic() : true);
        dto.setCategory(book.getCategory());
        dto.setActivityScore(book.getActivityScore() != null ? book.getActivityScore() : 0);
        
        if (book.getUser() != null) {
            dto.setUserId(book.getUser().getId());
            dto.setUsername(book.getUser().getUsername());
        }
        
        return dto;
    }

    public static List<BookDto> toBookDtoList(List<Book> books) {
        if (books == null) return List.of();
        return books.stream()
                .map(DtoMapper::toBookDto)
                .collect(Collectors.toList());
    }

    public static CommentDto toCommentDto(Comment comment) {
        if (comment == null) return null;
        
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setCreatedAt(comment.getCreatedAt());
        
        if (comment.getBook() != null) {
            dto.setBookId(comment.getBook().getId());
        }
        
        User author = comment.getAuthor();
        if (author != null) {
            dto.setAuthorId(author.getId());
            dto.setAuthorUsername(author.getUsername());
            dto.setAuthorDisplayName(author.getDisplayName());
            dto.setAuthorAvatarUrl(author.getAvatarUrl());
        }
        
        return dto;
    }

    public static List<CommentDto> toCommentDtoList(List<Comment> comments) {
        if (comments == null) return List.of();
        return comments.stream()
                .map(DtoMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    public static RatingDto toRatingDto(Rating rating) {
        if (rating == null) return null;
        
        RatingDto dto = new RatingDto();
        dto.setId(rating.getId());
        dto.setScore(rating.getScore());
        dto.setLiked(rating.getLiked() != null ? rating.getLiked() : false);
        
        if (rating.getBook() != null) {
            dto.setBookId(rating.getBook().getId());
        }
        
        if (rating.getUser() != null) {
            dto.setUserId(rating.getUser().getId());
            dto.setUsername(rating.getUser().getUsername());
        }
        
        return dto;
    }
}

