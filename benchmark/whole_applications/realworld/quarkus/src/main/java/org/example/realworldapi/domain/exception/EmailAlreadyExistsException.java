package org.example.realworldapi.domain.exception;

public class EmailAlreadyExistsException extends RuntimeException {
  public EmailAlreadyExistsException() {
    super("email already exists");
  }
}
