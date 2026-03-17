package org.example.realworldapi.domain.feature;

import java.util.UUID;
import org.example.realworldapi.domain.model.user.User;

public interface FindUserById {
  User handle(UUID id);
}
