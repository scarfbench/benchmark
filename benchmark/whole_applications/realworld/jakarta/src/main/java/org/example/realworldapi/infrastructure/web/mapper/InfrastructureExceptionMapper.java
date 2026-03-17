package org.example.realworldapi.infrastructure.web.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.example.realworldapi.application.web.model.response.ErrorResponse;
import org.example.realworldapi.infrastructure.web.exception.ForbiddenException;
import org.example.realworldapi.infrastructure.web.exception.InfrastructureException;
import org.example.realworldapi.infrastructure.web.exception.UnauthorizedException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Provider
public class InfrastructureExceptionMapper implements ExceptionMapper<InfrastructureException> {

    @Inject
    ObjectMapper objectMapper;

    private final Map<
            Class<? extends InfrastructureException>, Function<InfrastructureException, Response>>
            exceptionMapper;

    public InfrastructureExceptionMapper() {
        this.exceptionMapper = configureExceptionMapper();
    }

    private Map<Class<? extends InfrastructureException>, Function<InfrastructureException, Response>>
    configureExceptionMapper() {
        final var exceptionMap =
                new HashMap<
                        Class<? extends InfrastructureException>,
                        Function<InfrastructureException, Response>>();
        exceptionMap.put(ForbiddenException.class, this::forbidden);
        exceptionMap.put(UnauthorizedException.class, this::unauthorized);
        return exceptionMap;
    }

    private Response forbidden(InfrastructureException infrastructureException) {
        try {
            return Response.ok(objectMapper.writeValueAsString(errorResponse(Response.Status.FORBIDDEN.toString())))
                    .status(Response.Status.FORBIDDEN)
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Response unauthorized(InfrastructureException infrastructureException) {
        try {
            return Response.ok(objectMapper.writeValueAsString(errorResponse(Response.Status.UNAUTHORIZED.toString())))
                    .status(Response.Status.UNAUTHORIZED)
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private ErrorResponse errorResponse(String message) {
        return new ErrorResponse(message);
    }

    @Override
    public Response toResponse(InfrastructureException infrastructureException) {
        return this.exceptionMapper
                .get(infrastructureException.getClass())
                .apply(infrastructureException);
    }
}
