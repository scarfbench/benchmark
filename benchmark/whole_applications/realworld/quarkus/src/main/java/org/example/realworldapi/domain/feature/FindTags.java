package org.example.realworldapi.domain.feature;

import java.util.List;
import org.example.realworldapi.domain.model.tag.Tag;

public interface FindTags {
  List<Tag> handle();
}
