package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.exception.ArticleNotFoundException;
import org.example.realworldapi.domain.feature.FindArticleById;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.ArticleRepository;

import java.util.UUID;

@Singleton
public class FindArticleByIdImpl implements FindArticleById {

    @Inject
    private ArticleRepository articleRepository;

    @Override
    public Article handle(UUID id) {
        return articleRepository.findArticleById(id).orElseThrow(ArticleNotFoundException::new);
    }
}
