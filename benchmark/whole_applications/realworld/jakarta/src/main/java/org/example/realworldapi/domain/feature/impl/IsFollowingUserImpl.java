package org.example.realworldapi.domain.feature.impl;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.example.realworldapi.domain.feature.IsFollowingUser;
import org.example.realworldapi.domain.model.user.FollowRelationshipRepository;

import java.util.UUID;

@Dependent
public class IsFollowingUserImpl implements IsFollowingUser {

    @Inject
    private FollowRelationshipRepository usersFollowedRepository;

    @Override
    public boolean handle(UUID currentUserId, UUID followedUserId) {
        return usersFollowedRepository.isFollowing(currentUserId, followedUserId);
    }
}
