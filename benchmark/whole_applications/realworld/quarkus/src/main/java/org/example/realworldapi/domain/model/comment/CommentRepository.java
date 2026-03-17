package org.example.realworldapi.domain.model.comment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.realworldapi.domain.model.article.Article;

public interface CommentRepository {
  void save(Comment comment);

  Optional<Comment> findByIdAndAuthor(UUID commentId, UUID authorId);

  void delete(Comment comment);

  List<Comment> findCommentsByArticle(Article article);
}
