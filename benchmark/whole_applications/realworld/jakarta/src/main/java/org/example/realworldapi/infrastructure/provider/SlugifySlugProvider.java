package org.example.realworldapi.infrastructure.provider;

import com.github.slugify.Slugify;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.realworldapi.domain.model.provider.SlugProvider;


@ApplicationScoped
public class SlugifySlugProvider implements SlugProvider {

    private Slugify slugify;

    @Inject
    public SlugifySlugProvider() {
        this.slugify = Slugify.builder().build();
    }

    @Override
    public String slugify(String text) {
        return slugify.slugify(text);
    }
}
