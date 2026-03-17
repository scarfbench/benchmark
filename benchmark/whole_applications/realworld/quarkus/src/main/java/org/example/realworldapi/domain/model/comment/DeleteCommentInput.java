package org.example.realworldapi.domain.model.comment;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeleteCommentInput {
  private final UUID commentId;
  private final UUID authorId;
  private final String articleSlug;
}
