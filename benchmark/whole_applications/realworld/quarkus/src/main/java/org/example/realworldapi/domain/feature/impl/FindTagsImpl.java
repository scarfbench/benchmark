package org.example.realworldapi.domain.feature.impl;

import java.util.List;
import lombok.AllArgsConstructor;
import org.example.realworldapi.domain.feature.FindTags;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.domain.model.tag.TagRepository;

@AllArgsConstructor
public class FindTagsImpl implements FindTags {

  private final TagRepository tagRepository;

  @Override
  public List<Tag> handle() {
    return tagRepository.findAllTags();
  }
}
