package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.feature.CreateComment;
import org.example.realworldapi.domain.feature.FindArticleBySlug;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentBuilder;
import org.example.realworldapi.domain.model.comment.CommentRepository;
import org.example.realworldapi.domain.model.comment.NewCommentInput;

@Singleton
public class CreateCommentImpl implements CreateComment {

    @Inject
    private CommentRepository commentRepository;
    @Inject
    private FindUserById findUserById;
    @Inject
    private FindArticleBySlug findArticleBySlug;
    @Inject
    private CommentBuilder commentBuilder;

    @Override
    public Comment handle(NewCommentInput newCommentInput) {
        final var author = findUserById.handle(newCommentInput.getAuthorId());
        final var article = findArticleBySlug.handle(newCommentInput.getArticleSlug());
        final var comment = commentBuilder.build(author, article, newCommentInput.getBody());
        commentRepository.save(comment);
        return comment;
    }
}
