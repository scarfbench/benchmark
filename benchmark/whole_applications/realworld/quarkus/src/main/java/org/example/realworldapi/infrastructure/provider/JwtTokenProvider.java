package org.example.realworldapi.infrastructure.provider;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;

@ApplicationScoped
public class JwtTokenProvider {

  public String createUserToken(String subject) {
    return Jwt.issuer("users-service")
        .subject(subject)
        .groups(new HashSet<>(List.of("USER")))
        .sign();
  }
}
