package org.example.realworldapi.domain.model.article;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArticleFilter {
  private final int offset;
  private final int limit;
  private final UUID loggedUserId;
  private final List<String> tags;
  private final List<String> authors;
  private final List<String> favorited;
}
