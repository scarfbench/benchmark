package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.exception.EmailAlreadyExistsException;
import org.example.realworldapi.domain.exception.UsernameAlreadyExistsException;
import org.example.realworldapi.domain.feature.CreateUser;
import org.example.realworldapi.domain.model.provider.HashProvider;
import org.example.realworldapi.domain.model.user.CreateUserInput;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.domain.model.user.UserModelBuilder;
import org.example.realworldapi.domain.model.user.UserRepository;

@Singleton
public class CreateUserImpl implements CreateUser {

    @Inject
    private UserRepository userRepository;
    @Inject
    private HashProvider hashProvider;
    @Inject
    private UserModelBuilder userBuilder;

    @Override
    public User handle(CreateUserInput createUserInput) {
        final var user =
                userBuilder.build(
                        createUserInput.getUsername(),
                        createUserInput.getEmail(),
                        createUserInput.getPassword());
        checkExistingUsername(user.getUsername());
        checkExistingEmail(user.getEmail());
        user.setPassword(hashProvider.hashPassword(user.getPassword()));
        userRepository.save(user);
        return user;
    }

    private void checkExistingUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException();
        }
    }

    private void checkExistingEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }
    }
}
