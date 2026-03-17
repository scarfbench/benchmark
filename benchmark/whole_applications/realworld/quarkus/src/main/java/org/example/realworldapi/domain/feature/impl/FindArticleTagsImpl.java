package org.example.realworldapi.domain.feature.impl;

import java.util.List;
import lombok.AllArgsConstructor;
import org.example.realworldapi.domain.feature.FindArticleTags;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.TagRelationshipRepository;
import org.example.realworldapi.domain.model.tag.Tag;

@AllArgsConstructor
public class FindArticleTagsImpl implements FindArticleTags {

  private final TagRelationshipRepository tagRelationshipRepository;

  @Override
  public List<Tag> handle(Article article) {
    return tagRelationshipRepository.findArticleTags(article);
  }
}
