package org.example.realworldapi.domain.feature.impl;

import java.util.List;
import lombok.AllArgsConstructor;
import org.example.realworldapi.domain.feature.CreateArticle;
import org.example.realworldapi.domain.feature.CreateSlugByTitle;
import org.example.realworldapi.domain.feature.FindTagsByNameCreateIfNotExists;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.model.article.*;
import org.example.realworldapi.domain.model.tag.Tag;

@AllArgsConstructor
public class CreateArticleImpl implements CreateArticle {

  private final FindUserById findUserById;
  private final ArticleRepository articleRepository;
  private final ArticleModelBuilder articleBuilder;
  private final CreateSlugByTitle createSlugByTitle;
  private final FindTagsByNameCreateIfNotExists findTagsByNameCreateIfNotExists;
  private final TagRelationshipRepository tagRelationshipRepository;

  @Override
  public Article handle(NewArticleInput newArticleInput) {
    final var author = findUserById.handle(newArticleInput.authorId());
    final var slug = createSlugByTitle.handle(newArticleInput.title());
    final var article =
        articleBuilder.build(
            slug,
            newArticleInput.title(),
            newArticleInput.description(),
            newArticleInput.body(),
            author);
    articleRepository.save(article);
    final var tags = findTagsByNameCreateIfNotExists.handle(newArticleInput.tagList());
    createTagRelationship(article, tags);
    return article;
  }

  private void createTagRelationship(Article article, List<Tag> tags) {
    tags.forEach(tag -> tagRelationshipRepository.save(new TagRelationship(article, tag)));
  }
}
