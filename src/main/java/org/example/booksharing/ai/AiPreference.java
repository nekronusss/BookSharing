package org.example.booksharing.ai;

import jakarta.persistence.*;
import lombok.Data;
import org.example.booksharing.entities.User;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
@Table(name = "ai_preference")
public class AiPreference {
    @Id @Column(name = "user_id") private Long userId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
    @Column(nullable = false, length = 2000) private String genresJson = "[]";
    @Column(nullable = false, length = 500) private String interests = "";
    @Column(nullable = false) private boolean useHistory = false;
}
