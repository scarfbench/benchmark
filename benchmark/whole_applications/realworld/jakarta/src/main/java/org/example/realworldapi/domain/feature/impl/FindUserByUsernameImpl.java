package org.example.realworldapi.domain.feature.impl;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.example.realworldapi.domain.exception.UserNotFoundException;
import org.example.realworldapi.domain.feature.FindUserByUsername;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.domain.model.user.UserRepository;

@Dependent
public class FindUserByUsernameImpl implements FindUserByUsername {

    @Inject
    private UserRepository userRepository;

    @Override
    public User handle(String username) {
        return userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
    }
}
