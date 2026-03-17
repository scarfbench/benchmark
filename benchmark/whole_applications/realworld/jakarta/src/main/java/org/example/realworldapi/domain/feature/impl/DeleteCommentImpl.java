package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.feature.DeleteComment;
import org.example.realworldapi.domain.feature.FindCommentByIdAndAuthor;
import org.example.realworldapi.domain.model.comment.CommentRepository;
import org.example.realworldapi.domain.model.comment.DeleteCommentInput;

@Singleton
public class DeleteCommentImpl implements DeleteComment {

    @Inject
    private FindCommentByIdAndAuthor findCommentByIdAndAuthor;
    @Inject
    private CommentRepository commentRepository;

    @Override
    public void handle(DeleteCommentInput deleteCommentInput) {
        final var comment =
                findCommentByIdAndAuthor.handle(
                        deleteCommentInput.getCommentId(), deleteCommentInput.getAuthorId());
        commentRepository.delete(comment);
    }
}
