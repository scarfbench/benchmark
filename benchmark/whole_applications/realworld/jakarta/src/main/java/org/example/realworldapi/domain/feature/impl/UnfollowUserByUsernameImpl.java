package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.feature.FindUserByUsername;
import org.example.realworldapi.domain.feature.UnfollowUserByUsername;
import org.example.realworldapi.domain.model.user.FollowRelationshipRepository;

import java.util.UUID;

@Singleton
public class UnfollowUserByUsernameImpl implements UnfollowUserByUsername {

    @Inject
    private FindUserById findUserById;
    @Inject
    private FindUserByUsername findUserByUsername;
    @Inject
    private FollowRelationshipRepository followRelationshipRepository;

    @Override
    public void handle(UUID loggedUserId, String username) {
        final var loggedUser = findUserById.handle(loggedUserId);
        final var userToUnfollow = findUserByUsername.handle(username);
        final var followingRelationship =
                followRelationshipRepository.findByUsers(loggedUser, userToUnfollow).orElseThrow();
        followRelationshipRepository.remove(followingRelationship);
    }
}
