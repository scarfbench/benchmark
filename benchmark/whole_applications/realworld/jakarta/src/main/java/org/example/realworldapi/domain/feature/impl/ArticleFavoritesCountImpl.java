package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.feature.ArticleFavoritesCount;
import org.example.realworldapi.domain.feature.FindArticleById;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;

import java.util.UUID;

@Singleton
public class ArticleFavoritesCountImpl implements ArticleFavoritesCount {

    @Inject
    private FindArticleById findArticleById;
    @Inject
    private FavoriteRelationshipRepository favoriteRelationshipRepository;

    @Override
    public long handle(UUID articleId) {
        final var article = findArticleById.handle(articleId);
        return favoriteRelationshipRepository.favoritesCount(article);
    }
}
