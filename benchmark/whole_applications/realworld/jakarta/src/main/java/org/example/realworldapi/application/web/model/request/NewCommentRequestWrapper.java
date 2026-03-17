package org.example.realworldapi.application.web.model.request;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewCommentRequestWrapper {

    @Valid
    private NewCommentRequest comment;
}
