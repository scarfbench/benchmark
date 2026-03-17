package org.example.realworldapi.domain.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
  public UsernameAlreadyExistsException() {
    super("username already exists");
  }
}
