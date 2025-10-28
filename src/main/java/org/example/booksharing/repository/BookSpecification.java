package org.example.booksharing.repository;

import jakarta.persistence.criteria.Predicate;
import org.example.booksharing.entities.Book;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {
    public static Specification<Book> filter(String search, String author, Integer ratingMin, Integer ratingMax) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null)
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"));
            if (author != null)
                predicates.add(cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%"));
            if (ratingMin != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), ratingMin));
            if (ratingMax != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("rating"), ratingMax));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
