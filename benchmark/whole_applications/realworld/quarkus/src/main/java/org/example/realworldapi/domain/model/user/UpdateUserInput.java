package org.example.realworldapi.domain.model.user;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateUserInput {
  private UUID id;
  private String username;
  private String bio;
  private String image;
  private String email;
}
