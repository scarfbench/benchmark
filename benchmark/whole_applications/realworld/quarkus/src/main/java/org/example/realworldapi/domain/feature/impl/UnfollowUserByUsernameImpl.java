package org.example.realworldapi.domain.feature.impl;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.feature.FindUserByUsername;
import org.example.realworldapi.domain.feature.UnfollowUserByUsername;
import org.example.realworldapi.domain.model.user.FollowRelationshipRepository;

@AllArgsConstructor
public class UnfollowUserByUsernameImpl implements UnfollowUserByUsername {

  private final FindUserById findUserById;
  private final FindUserByUsername findUserByUsername;
  private final FollowRelationshipRepository followRelationshipRepository;

  @Override
  public void handle(UUID loggedUserId, String username) {
    final var loggedUser = findUserById.handle(loggedUserId);
    final var userToUnfollow = findUserByUsername.handle(username);
    final var followingRelationship =
        followRelationshipRepository.findByUsers(loggedUser, userToUnfollow).orElseThrow();
    followRelationshipRepository.remove(followingRelationship);
  }
}
