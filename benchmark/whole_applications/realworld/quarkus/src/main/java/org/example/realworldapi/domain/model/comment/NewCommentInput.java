package org.example.realworldapi.domain.model.comment;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewCommentInput {
  private UUID authorId;
  private String articleSlug;
  private String body;
}
