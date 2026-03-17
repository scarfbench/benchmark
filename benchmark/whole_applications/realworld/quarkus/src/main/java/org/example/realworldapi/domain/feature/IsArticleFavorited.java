package org.example.realworldapi.domain.feature;

import java.util.UUID;
import org.example.realworldapi.domain.model.article.Article;

public interface IsArticleFavorited {
  boolean handle(Article article, UUID currentUserId);
}
