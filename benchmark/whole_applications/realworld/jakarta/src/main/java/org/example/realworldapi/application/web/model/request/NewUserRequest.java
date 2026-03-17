package org.example.realworldapi.application.web.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.realworldapi.domain.model.constants.ValidationMessages;
import org.example.realworldapi.domain.model.user.CreateUserInput;

@Getter
@Setter
@NotNull(message = ValidationMessages.REQUEST_BODY_MUST_BE_NOT_NULL)
public class NewUserRequest {
    private String username;
    private String email;
    private String password;

    public CreateUserInput toCreateUserInput() {
        return new CreateUserInput(this.username, this.email, this.password);
    }
}
