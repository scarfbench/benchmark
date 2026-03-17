package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.feature.FindArticleTags;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.TagRelationshipRepository;
import org.example.realworldapi.domain.model.tag.Tag;

import java.util.List;

@Singleton
public class FindArticleTagsImpl implements FindArticleTags {

    @Inject
    private TagRelationshipRepository tagRelationshipRepository;

    @Override
    public List<Tag> handle(Article article) {
        return tagRelationshipRepository.findArticleTags(article);
    }
}
