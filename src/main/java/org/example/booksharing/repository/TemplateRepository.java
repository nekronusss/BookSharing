package org.example.booksharing.repository;

import org.example.booksharing.entities.Template;
import org.example.booksharing.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    List<Template> findByUser(User user);
}

