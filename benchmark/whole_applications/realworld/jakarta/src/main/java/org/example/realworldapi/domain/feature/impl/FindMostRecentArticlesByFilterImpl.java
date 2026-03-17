package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.feature.FindMostRecentArticlesByFilter;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.ArticleFilter;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.example.realworldapi.domain.model.article.PageResult;

@Singleton
public class FindMostRecentArticlesByFilterImpl implements FindMostRecentArticlesByFilter {

    @Inject
    private ArticleRepository articleRepository;

    @Override
    public PageResult<Article> handle(ArticleFilter articleFilter) {
        return articleRepository.findMostRecentArticlesByFilter(articleFilter);
    }
}
