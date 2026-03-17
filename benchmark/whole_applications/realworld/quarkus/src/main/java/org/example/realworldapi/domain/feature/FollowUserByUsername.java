package org.example.realworldapi.domain.feature;

import java.util.UUID;
import org.example.realworldapi.domain.model.user.FollowRelationship;

public interface FollowUserByUsername {
  FollowRelationship handle(UUID loggedUserId, String username);
}
