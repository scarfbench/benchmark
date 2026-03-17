package org.example.realworldapi.domain.feature;

import java.util.UUID;
import org.example.realworldapi.domain.model.article.Article;

public interface FindArticleById {
  Article handle(UUID id);
}
