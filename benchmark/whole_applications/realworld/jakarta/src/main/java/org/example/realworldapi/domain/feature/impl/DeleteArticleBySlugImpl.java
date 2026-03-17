package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.feature.DeleteArticleBySlug;
import org.example.realworldapi.domain.feature.FindArticleByAuthorAndSlug;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.example.realworldapi.domain.model.article.FavoriteRelationship;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;
import org.example.realworldapi.domain.model.article.TagRelationshipRepository;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class DeleteArticleBySlugImpl implements DeleteArticleBySlug {

    @Inject
    private FindArticleByAuthorAndSlug findArticleByAuthorAndSlug;
    @Inject
    private ArticleRepository articleRepository;
    @Inject
    private TagRelationshipRepository tagRelationshipRepository;
    @Inject
    private CommentRepository commentRepository;
    @Inject
    private FavoriteRelationshipRepository favoriteRelationshipRepository;

    @Override
    public void handle(UUID authorId, String slug) {
        final var article = findArticleByAuthorAndSlug.handle(authorId, slug);
        tagRelationshipRepository.delete(article);
        Optional<FavoriteRelationship> favorite = favoriteRelationshipRepository.findByArticleIdAndUserId(article.getId(), authorId);
        favorite.ifPresent(favoriteRelationship -> favoriteRelationshipRepository.delete(favoriteRelationship));
        List<Comment> comments = commentRepository.findCommentsByArticle(article);
        comments.forEach(c -> commentRepository.delete(c));
        articleRepository.delete(article);
    }
}
