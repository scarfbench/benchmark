package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.example.realworldapi.application.web.resource.utils.ResourceUtils;
import org.example.realworldapi.domain.feature.FollowUserByUsername;
import org.example.realworldapi.domain.feature.UnfollowUserByUsername;
import org.example.realworldapi.domain.model.constants.ValidationMessages;
import org.example.realworldapi.infrastructure.web.security.annotation.Secured;
import org.example.realworldapi.infrastructure.web.security.profile.Role;

@Path("/profiles")
public class ProfilesResource {

    @Inject
    private FollowUserByUsername followUserByUsername;
    @Inject
    private UnfollowUserByUsername unfollowUserByUsername;
    @Inject
    private ResourceUtils resourceUtils;
    @Inject
    private ObjectMapper objectMapper;

    @GET
    @Secured(optional = true)
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfile(
            @PathParam("username") @NotBlank(message = ValidationMessages.USERNAME_MUST_BE_NOT_BLANK)
            String username,
            @Context SecurityContext securityContext) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
        final var profileResponse = resourceUtils.profileResponse(username, loggedUserId);
        return Response.ok(objectMapper.writeValueAsString(profileResponse)).status(Response.Status.OK).build();
    }

    @POST
    @Transactional
    @Secured({Role.USER, Role.ADMIN})
    @Path("/{username}/follow")
    @Produces(MediaType.APPLICATION_JSON)
    public Response follow(
            @PathParam("username") @NotBlank(message = ValidationMessages.USERNAME_MUST_BE_NOT_BLANK)
            String username,
            @Context SecurityContext securityContext) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
        followUserByUsername.handle(loggedUserId, username);
        return Response.ok(objectMapper.writeValueAsString(resourceUtils.profileResponse(username, loggedUserId)))
                .status(Response.Status.OK)
                .build();
    }

    @DELETE
    @Transactional
    @Secured({Role.USER, Role.ADMIN})
    @Path("/{username}/follow")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unfollow(
            @PathParam("username") @NotBlank(message = ValidationMessages.USERNAME_MUST_BE_NOT_BLANK)
            String username,
            @Context SecurityContext securityContext) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
        unfollowUserByUsername.handle(loggedUserId, username);
        return Response.ok(objectMapper.writeValueAsString(resourceUtils.profileResponse(username, loggedUserId)))
                .status(Response.Status.OK)
                .build();
    }
}
