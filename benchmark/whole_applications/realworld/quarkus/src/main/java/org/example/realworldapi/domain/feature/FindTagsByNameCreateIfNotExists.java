package org.example.realworldapi.domain.feature;

import java.util.List;
import org.example.realworldapi.domain.model.tag.Tag;

public interface FindTagsByNameCreateIfNotExists {
  List<Tag> handle(List<String> names);
}
