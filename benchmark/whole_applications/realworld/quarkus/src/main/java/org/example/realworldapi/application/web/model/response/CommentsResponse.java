package org.example.realworldapi.application.web.model.response;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class CommentsResponse {

  private List<CommentResponse> comments;

  public CommentsResponse(List<CommentResponse> comments) {
    this.comments = comments;
  }
}
