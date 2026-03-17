package org.example.realworldapi.infrastructure.web.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.example.realworldapi.application.web.model.response.ErrorResponse;
import org.example.realworldapi.domain.exception.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

    @Inject
    ObjectMapper objectMapper;

    private final Map<Class<? extends BusinessException>, Function<BusinessException, Response>>
            exceptionMapper;

    public BusinessExceptionMapper() {
        this.exceptionMapper = configureExceptionMapper();
    }

    private Map<Class<? extends BusinessException>, Function<BusinessException, Response>>
    configureExceptionMapper() {

        final var handlerMap =
                new HashMap<Class<? extends BusinessException>, Function<BusinessException, Response>>();

        handlerMap.put(EmailAlreadyExistsException.class, this::conflict);
        handlerMap.put(UserNotFoundException.class, this::notFound);
        handlerMap.put(InvalidPasswordException.class, this::unauthorized);
        handlerMap.put(UsernameAlreadyExistsException.class, this::conflict);
        handlerMap.put(TagNotFoundException.class, this::notFound);
        handlerMap.put(ArticleNotFoundException.class, this::notFound);
        handlerMap.put(ModelValidationException.class, this::unprocessableEntity);

        return handlerMap;
    }

    private Response notFound(BusinessException businessException) {
        try {
            return Response.ok(objectMapper.writeValueAsString(errorResponse(businessException)))
                    .status(Response.Status.NOT_FOUND.getStatusCode())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Response conflict(BusinessException businessException) {
        try {
            return Response.ok(objectMapper.writeValueAsString(errorResponse(businessException)))
                    .status(Response.Status.CONFLICT.getStatusCode())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Response unauthorized(BusinessException businessException) {
        try {
            return Response.ok(objectMapper.writeValueAsString(errorResponse(businessException)))
                    .status(Response.Status.UNAUTHORIZED.getStatusCode())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Response unprocessableEntity(BusinessException businessException) {
        try {
            return Response.ok(objectMapper.writeValueAsString(errorResponse(businessException))).status(422).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private ErrorResponse errorResponse(BusinessException businessException) {
        return new ErrorResponse(businessException.getMessages());
    }

    @Override
    public Response toResponse(BusinessException businessException) {
        return this.exceptionMapper.get(businessException.getClass()).apply(businessException);
    }
}
