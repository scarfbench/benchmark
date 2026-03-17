package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.feature.CreateTag;
import org.example.realworldapi.domain.feature.FindTagsByNameCreateIfNotExists;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.domain.model.tag.TagRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class FindTagsByNameCreateIfNotExistsImpl implements FindTagsByNameCreateIfNotExists {

    @Inject
    private TagRepository tagRepository;
    @Inject
    private CreateTag createTag;

    @Override
    public List<Tag> handle(List<String> names) {
        final var tags = tagRepository.findByNames(names);
        tags.addAll(createTags(nonexistent(tags, names)));
        return tags;
    }

    private List<Tag> createTags(List<String> names) {
        final var tags = new LinkedList<Tag>();
        names.forEach(name -> tags.add(createTag.handle(name)));
        return tags;
    }

    private List<String> nonexistent(List<Tag> existing, List<String> allNames) {
        return allNames.stream()
                .filter(name -> existing.stream().noneMatch(tag -> tag.getName().equalsIgnoreCase(name)))
                .collect(Collectors.toList());
    }
}
