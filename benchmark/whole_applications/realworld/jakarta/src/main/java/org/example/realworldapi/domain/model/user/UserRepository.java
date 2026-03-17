package org.example.realworldapi.domain.model.user;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    void save(User user);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findUserById(UUID id);

    boolean existsUsername(UUID excludeId, String username);

    boolean existsEmail(UUID excludeId, String email);

    void update(User user);

    Optional<User> findByUsername(String username);
}
