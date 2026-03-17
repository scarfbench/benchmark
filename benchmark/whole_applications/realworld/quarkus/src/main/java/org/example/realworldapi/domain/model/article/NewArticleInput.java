package org.example.realworldapi.domain.model.article;

import java.util.List;
import java.util.UUID;

public record NewArticleInput(
    UUID authorId, String title, String description, String body, List<String> tagList) {}
