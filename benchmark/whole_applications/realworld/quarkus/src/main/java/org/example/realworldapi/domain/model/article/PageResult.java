package org.example.realworldapi.domain.model.article;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageResult<T> {
  private List<T> result;
  private long total;
}
