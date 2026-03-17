package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.feature.FindArticleBySlug;
import org.example.realworldapi.domain.feature.UnfavoriteArticle;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;

import java.util.UUID;

@Singleton
public class UnfavoriteArticleImpl implements UnfavoriteArticle {

    @Inject
    private FindArticleBySlug findArticleBySlug;
    @Inject
    private FavoriteRelationshipRepository favoriteRelationshipRepository;

    @Override
    public void handle(String articleSlug, UUID currentUserId) {
        final var article = findArticleBySlug.handle(articleSlug);
        final var favoriteRelationshipOptional =
                favoriteRelationshipRepository.findByArticleIdAndUserId(article.getId(), currentUserId);
        favoriteRelationshipOptional.ifPresent(favoriteRelationshipRepository::delete);
    }
}
