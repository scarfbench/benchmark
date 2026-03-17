package org.example.realworldapi.domain.model.user;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.validator.ModelValidator;

import java.util.UUID;

@Singleton
@Named("usermodelbuilder")
public class UserModelBuilder {

    @Inject
    private ModelValidator modelValidator;

    public User build(String username, String email, String password) {
        return modelValidator.validate(
                new User(UUID.randomUUID(), username, email, password, null, null));
    }

    public User build(
            UUID id, String username, String bio, String image, String password, String email) {
        return modelValidator.validate(new User(id, username, email, password, bio, image));
    }
}
