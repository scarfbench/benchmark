package org.example.realworldapi.domain.model.article;

import java.util.List;
import org.example.realworldapi.domain.model.tag.Tag;

public interface TagRelationshipRepository {
  void save(TagRelationship tagRelationship);

  List<Tag> findArticleTags(Article article);
}
