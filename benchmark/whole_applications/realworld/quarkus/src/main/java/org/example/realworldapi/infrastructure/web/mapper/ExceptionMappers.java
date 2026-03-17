package org.example.realworldapi.infrastructure.web.mapper;

import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.core.Response;
import org.example.realworldapi.application.web.model.response.ErrorResponse;
import org.example.realworldapi.domain.exception.*;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMappers {

  @ServerExceptionMapper
  public Response unauthorized(UnauthorizedException e) {
    return Response.ok(errorResponse("Unauthorized"))
        .status(Response.Status.UNAUTHORIZED.getStatusCode())
        .build();
  }

  @ServerExceptionMapper
  public Response forbidden(ForbiddenException e) {
    return Response.ok(errorResponse("Forbidden"))
        .status(Response.Status.FORBIDDEN.getStatusCode())
        .build();
  }

  @ServerExceptionMapper
  public Response emailAlreadyExists(EmailAlreadyExistsException e) {
    return Response.ok(errorResponse(e)).status(Response.Status.CONFLICT.getStatusCode()).build();
  }

  @ServerExceptionMapper
  public Response usernameAlreadyExists(UsernameAlreadyExistsException e) {
    return Response.ok(errorResponse(e)).status(Response.Status.CONFLICT.getStatusCode()).build();
  }

  @ServerExceptionMapper
  public Response userNotfound(UserNotFoundException e) {
    return Response.ok(errorResponse(e)).status(Response.Status.NOT_FOUND.getStatusCode()).build();
  }

  @ServerExceptionMapper
  public Response invalidPassword(InvalidPasswordException e) {
    return Response.ok(errorResponse(e))
        .status(Response.Status.UNAUTHORIZED.getStatusCode())
        .build();
  }

  @ServerExceptionMapper
  public Response tagNotFound(TagNotFoundException e) {
    return Response.ok(errorResponse(e)).status(Response.Status.NOT_FOUND.getStatusCode()).build();
  }

  @ServerExceptionMapper
  public Response articleNotFound(ArticleNotFoundException e) {
    return Response.ok(errorResponse(e)).status(Response.Status.NOT_FOUND.getStatusCode()).build();
  }

  @ServerExceptionMapper
  public Response modelValidation(ModelValidationException e) {
    return Response.ok(errorResponse(e)).status(422).build();
  }

  private ErrorResponse errorResponse(String e) {
    return new ErrorResponse(e);
  }

  private ErrorResponse errorResponse(RuntimeException e) {
    return new ErrorResponse(e.getMessage());
  }
}
