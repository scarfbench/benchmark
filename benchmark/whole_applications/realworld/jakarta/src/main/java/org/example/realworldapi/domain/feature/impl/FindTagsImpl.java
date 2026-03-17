package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.feature.FindTags;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.domain.model.tag.TagRepository;

import java.util.List;

@Singleton
public class FindTagsImpl implements FindTags {

    @Inject
    private TagRepository tagRepository;

    @Override
    public List<Tag> handle() {
        return tagRepository.findAllTags();
    }
}
