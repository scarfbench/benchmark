package org.example.realworldapi.util;

import io.quarkus.elytron.security.common.BcryptUtil;
import java.util.UUID;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.UserEntity;

public class UserEntityUtils {

  public static UserEntity create(String username, String email, String userPassword) {
    final var userEntity = new UserEntity();
    userEntity.setId(UUID.randomUUID());
    userEntity.setUsername(username);
    userEntity.setEmail(email);
    userEntity.setPassword(BcryptUtil.bcryptHash(userPassword));
    return userEntity;
  }

  public static UserEntity create(UUID id, String username, String email, String userPassword) {
    final var userEntity = create(username, email, userPassword);
    userEntity.setId(id);
    return userEntity;
  }

  public static UserEntity create(
      String username, String email, String userPassword, String bio, String image) {
    final var userEntity = create(username, email, userPassword);
    userEntity.setBio(bio);
    userEntity.setImage(image);
    return userEntity;
  }
}
