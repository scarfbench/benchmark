package org.example.realworldapi.infrastructure.web.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.example.realworldapi.application.web.model.response.ErrorResponse;

@Provider
public class BeanValidationExceptionMapper
        implements ExceptionMapper<ConstraintViolationException> {

    @Inject
    ObjectMapper objectMapper;

    @Override
    public Response toResponse(ConstraintViolationException e) {

        ErrorResponse errorResponse = new ErrorResponse();

        e.getConstraintViolations()
                .iterator()
                .forEachRemaining(
                        contraint -> {
                            errorResponse.getBody().add(contraint.getMessage());
                        });

        try {
            return Response.ok(objectMapper.writeValueAsString(errorResponse)).status(422).build();
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
