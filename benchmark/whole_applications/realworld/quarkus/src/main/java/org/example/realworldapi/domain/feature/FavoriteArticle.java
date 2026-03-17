package org.example.realworldapi.domain.feature;

import java.util.UUID;
import org.example.realworldapi.domain.model.article.FavoriteRelationship;

public interface FavoriteArticle {
  FavoriteRelationship handle(String articleSlug, UUID currentUserId);
}
