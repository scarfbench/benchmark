package org.example.realworldapi.application.web.model.request;

import jakarta.json.bind.annotation.JsonbNillable;
import lombok.Getter;
import lombok.Setter;
import org.example.realworldapi.domain.model.article.NewArticleInput;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class NewArticleRequest {

    private String title;
    private String description;
    private String body;
    private List<String> tagList;

    public NewArticleInput toNewArticleInput(UUID loggedUserId) {
        return new NewArticleInput(loggedUserId, this.title, this.description, this.body, this.tagList);
    }
}
