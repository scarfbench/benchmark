package org.example.realworldapi.domain.feature;

import java.util.List;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.tag.Tag;

public interface FindArticleTags {
  List<Tag> handle(Article article);
}
