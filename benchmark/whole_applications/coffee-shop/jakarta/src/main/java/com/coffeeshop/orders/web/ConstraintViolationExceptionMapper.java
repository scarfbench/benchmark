package com.coffeeshop.orders.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.Map;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

  @Override
  public Response toResponse(ConstraintViolationException ex) {
    List<Map<String, String>> errors = ex.getConstraintViolations().stream()
        .map(this::toEntry)
        .toList();
    return Response.status(Response.Status.BAD_REQUEST)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .entity(Map.of("errors", errors))
        .build();
  }

  private Map<String, String> toEntry(ConstraintViolation<?> v) {
    String field = v.getPropertyPath() == null ? "" : v.getPropertyPath().toString();
    return Map.of("field", field, "message", v.getMessage());
  }
}
