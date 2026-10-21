package org.example.booksharing.ai;

import jakarta.persistence.*;
import lombok.Data;
import org.example.booksharing.entities.Book;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
@Table(name = "ai_book_embedding")
public class BookEmbedding {
    @Id @Column(name = "book_id") private Long bookId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Book book;
    @Column(nullable = false, length = 64) private String contentHash;
    @Column(nullable = false, length = 64) private String modelKey;
    @Column(nullable = false, columnDefinition = "text") private String vectorJson;
}
