package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.example.realworldapi.application.web.model.request.UpdateUserRequestWrapper;
import org.example.realworldapi.application.web.model.response.UserResponse;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.feature.UpdateUser;
import org.example.realworldapi.infrastructure.web.provider.TokenProvider;
import org.example.realworldapi.infrastructure.web.security.annotation.Secured;
import org.example.realworldapi.infrastructure.web.security.profile.Role;

import java.util.UUID;

@Path("/user")
public class UserResource {

    @Inject
    private FindUserById findUserById;
    @Inject
    private UpdateUser updateUser;
    @Inject
    private TokenProvider tokenProvider;
    @Inject
    private ObjectMapper objectMapper;

    @GET
    @Secured({Role.ADMIN, Role.USER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@Context SecurityContext securityContext) throws JsonProcessingException {
        final var userId = UUID.fromString(securityContext.getUserPrincipal().getName());
        final var user = findUserById.handle(userId);
        final var token = tokenProvider.createUserToken(user.getId().toString());
        return Response.ok(objectMapper.writeValueAsString(new UserResponse(user, token))).status(Response.Status.OK).build();
    }

    @PUT
    @Transactional
    @Secured({Role.ADMIN, Role.USER})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @Context SecurityContext securityContext,
            @Valid UpdateUserRequestWrapper updateUserRequest) throws JsonProcessingException {
        final var userId = UUID.fromString(securityContext.getUserPrincipal().getName());
        final var user = updateUser.handle(updateUserRequest.getUser().toUpdateUserInput(userId));
        final var token = tokenProvider.createUserToken(user.getId().toString());
        return Response.ok(objectMapper.writeValueAsString(new UserResponse(user, token))).status(Response.Status.OK).build();
    }
}
