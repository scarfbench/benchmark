package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.feature.CreateSlugByTitle;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.example.realworldapi.domain.model.provider.SlugProvider;

import java.util.UUID;

@Singleton
public class CreateSlugByTitleImpl implements CreateSlugByTitle {

    @Inject
    private ArticleRepository articleRepository;
    @Inject
    private SlugProvider slugProvider;

    @Override
    public String handle(String title) {
        String slug = slugProvider.slugify(title);
        if (articleRepository.existsBySlug(slug)) {
            slug += UUID.randomUUID().toString();
        }
        return slug;
    }
}
