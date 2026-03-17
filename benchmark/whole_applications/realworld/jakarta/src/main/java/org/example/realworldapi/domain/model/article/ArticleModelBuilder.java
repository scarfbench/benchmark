package org.example.realworldapi.domain.model.article;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.domain.validator.ModelValidator;

import java.time.LocalDateTime;
import java.util.UUID;

@Singleton
public class ArticleModelBuilder {

    @Inject
    private ModelValidator modelValidator;

    public Article build(String slug, String title, String description, String body, User author) {
        final var createdAt = LocalDateTime.now();
        return modelValidator.validate(
                new Article(
                        UUID.randomUUID(), slug, title, description, body, createdAt, createdAt, author));
    }

    public Article build(
            UUID id,
            String slug,
            String title,
            String description,
            String body,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            User author) {
        return modelValidator.validate(
                new Article(id, slug, title, description, body, createdAt, updatedAt, author));
    }
}
