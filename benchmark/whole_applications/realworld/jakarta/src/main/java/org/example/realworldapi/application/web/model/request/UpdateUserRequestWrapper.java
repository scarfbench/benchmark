package org.example.realworldapi.application.web.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.realworldapi.domain.model.constants.ValidationMessages;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequestWrapper {

    @Valid
    @NotNull(message = ValidationMessages.REQUEST_BODY_MUST_BE_NOT_NULL)
    private UpdateUserRequest user;

}
