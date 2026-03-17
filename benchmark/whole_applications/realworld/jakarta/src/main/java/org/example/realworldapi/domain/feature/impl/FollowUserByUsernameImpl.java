package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.feature.FindUserByUsername;
import org.example.realworldapi.domain.feature.FollowUserByUsername;
import org.example.realworldapi.domain.model.user.FollowRelationship;
import org.example.realworldapi.domain.model.user.FollowRelationshipRepository;

import java.util.UUID;

@Singleton
public class FollowUserByUsernameImpl implements FollowUserByUsername {

    @Inject
    private FindUserById findUserById;
    @Inject
    private FindUserByUsername findUserByUsername;
    @Inject
    private FollowRelationshipRepository usersFollowedRepository;

    @Override
    public FollowRelationship handle(UUID loggedUserId, String username) {
        final var loggedUser = findUserById.handle(loggedUserId);
        final var userToFollow = findUserByUsername.handle(username);
        final var followingRelationship = new FollowRelationship(loggedUser, userToFollow);
        usersFollowedRepository.save(followingRelationship);
        return followingRelationship;
    }
}
