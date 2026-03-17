package org.example.realworldapi.domain.feature;

import java.util.UUID;
import org.example.realworldapi.domain.model.comment.Comment;

public interface FindCommentByIdAndAuthor {
  Comment handle(UUID commentId, UUID authorId);
}
