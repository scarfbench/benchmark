package org.example.realworldapi.domain.feature;

import java.util.List;
import org.example.realworldapi.domain.model.comment.Comment;

public interface FindCommentsByArticleSlug {
  List<Comment> handle(String slug);
}
