package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.exception.CommentNotFoundException;
import org.example.realworldapi.domain.feature.FindCommentByIdAndAuthor;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentRepository;

import java.util.UUID;

@Singleton
public class FindCommentByIdAndAuthorImpl implements FindCommentByIdAndAuthor {

    @Inject
    private CommentRepository commentRepository;

    @Override
    public Comment handle(UUID commentId, UUID authorId) {
        return commentRepository
                .findByIdAndAuthor(commentId, authorId)
                .orElseThrow(CommentNotFoundException::new);
    }
}
