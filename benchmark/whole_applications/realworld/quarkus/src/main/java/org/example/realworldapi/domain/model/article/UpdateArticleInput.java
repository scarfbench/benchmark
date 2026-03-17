package org.example.realworldapi.domain.model.article;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateArticleInput {
  private final UUID authorId;
  private final String slug;
  private final String title;
  private final String description;
  private final String body;
}
