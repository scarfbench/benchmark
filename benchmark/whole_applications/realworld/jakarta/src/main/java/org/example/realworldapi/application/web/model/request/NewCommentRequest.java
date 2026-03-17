package org.example.realworldapi.application.web.model.request;

import jakarta.json.bind.annotation.JsonbNillable;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.example.realworldapi.domain.model.comment.NewCommentInput;
import org.example.realworldapi.domain.model.constants.ValidationMessages;

import java.util.UUID;

@Getter
@Setter
public class NewCommentRequest {
    @NotBlank(message = ValidationMessages.BODY_MUST_BE_NOT_BLANK)
    private String body;

    public NewCommentInput toNewCommentInput(UUID authorId, String articleSlug) {
        return new NewCommentInput(authorId, articleSlug, this.body);
    }
}
