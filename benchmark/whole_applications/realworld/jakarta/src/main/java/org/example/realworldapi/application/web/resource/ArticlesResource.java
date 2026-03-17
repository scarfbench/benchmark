package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.example.realworldapi.application.web.model.request.NewArticleRequestWrapper;
import org.example.realworldapi.application.web.model.request.NewCommentRequestWrapper;
import org.example.realworldapi.application.web.model.request.UpdateArticleRequestWrapper;
import org.example.realworldapi.application.web.resource.utils.ResourceUtils;
import org.example.realworldapi.domain.feature.*;
import org.example.realworldapi.domain.model.article.ArticleFilter;
import org.example.realworldapi.domain.model.comment.DeleteCommentInput;
import org.example.realworldapi.domain.model.constants.ValidationMessages;
import org.example.realworldapi.infrastructure.web.qualifiers.NoWrapRootValueObjectMapper;
import org.example.realworldapi.infrastructure.web.security.annotation.Secured;
import org.example.realworldapi.infrastructure.web.security.profile.Role;

import java.util.List;
import java.util.UUID;

@Path("/articles")
public class ArticlesResource {

    @Inject
    private FindArticlesByFilter findArticlesByFilter;
    @Inject
    private CreateArticle createArticle;
    @Inject
    private FindMostRecentArticlesByFilter findMostRecentArticlesByFilter;
    @Inject
    private FindArticleBySlug findArticleBySlug;
    @Inject
    private UpdateArticleBySlug updateArticleBySlug;
    @Inject
    private DeleteArticleBySlug deleteArticleBySlug;
    @Inject
    private CreateComment createComment;
    @Inject
    private DeleteComment deleteComment;
    @Inject
    private FindCommentsByArticleSlug findCommentsByArticleSlug;
    @Inject
    private FavoriteArticle favoriteArticle;
    @Inject
    private UnfavoriteArticle unfavoriteArticle;
    @Inject
    private ResourceUtils resourceUtils;
    @Inject
    ObjectMapper objectMapper;
    @Inject
    @NoWrapRootValueObjectMapper
    ObjectMapper noWrapObjectMapper;


    @GET
    @Path("/feed")
    @Secured({Role.USER, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response feed(
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit,
            @Context SecurityContext securityContext) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
        final var articlesFilter =
                new ArticleFilter(offset, resourceUtils.getLimit(limit), loggedUserId, null, null, null);
        final var articlesPageResult = findMostRecentArticlesByFilter.handle(articlesFilter);
        return Response.ok(noWrapObjectMapper.writeValueAsString(resourceUtils.articlesResponse(articlesPageResult, loggedUserId)))
                .status(Response.Status.OK)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Secured(optional = true)
    public Response getArticles(
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit,
            @QueryParam("tag") List<String> tags,
            @QueryParam("author") List<String> authors,
            @QueryParam("favorited") List<String> favorited,
            @Context SecurityContext securityContext) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
        final var filter =
                new ArticleFilter(
                        offset, resourceUtils.getLimit(limit), loggedUserId, tags, authors, favorited);
        final var articlesPageResult = findArticlesByFilter.handle(filter);
        return Response.ok(noWrapObjectMapper.writeValueAsString(resourceUtils.articlesResponse(articlesPageResult, loggedUserId)))
                .status(Response.Status.OK)
                .build();
    }

    @POST
    @Transactional
    @Secured({Role.ADMIN, Role.USER})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
            @Valid
            NewArticleRequestWrapper newArticleRequest,
            @Context SecurityContext securityContext) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
        final var article = createArticle.handle(newArticleRequest.getArticle().toNewArticleInput(loggedUserId));
        return Response.ok(objectMapper.writeValueAsString(resourceUtils.articleResponse(article, loggedUserId)))
                .status(Response.Status.CREATED)
                .build();
    }

    @GET
    @Path("/{slug}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findBySlug(
            @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK)
            String slug) throws JsonProcessingException {
        final var article = findArticleBySlug.handle(slug);
        return Response.ok(objectMapper.writeValueAsString(resourceUtils.articleResponse(article, null)))
                .status(Response.Status.OK)
                .build();
    }

    @PUT
    @Transactional
    @Path("/{slug}")
    @Secured({Role.ADMIN, Role.USER})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("slug") @NotBlank String slug,
            @Valid UpdateArticleRequestWrapper updateArticleRequest,
            @Context SecurityContext securityContext) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
        final var updatedArticle =
                updateArticleBySlug.handle(updateArticleRequest.getArticle().toUpdateArticleInput(loggedUserId, slug));
        return Response.ok(objectMapper.writeValueAsString(resourceUtils.articleResponse(updatedArticle, null)))
                .status(Response.Status.OK)
                .build();
    }

    @DELETE
    @Transactional
    @Path("/{slug}")
    @Secured({Role.ADMIN, Role.USER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
            @Context SecurityContext securityContext) {
        final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
        deleteArticleBySlug.handle(loggedUserId, slug);
        return Response.ok().build();
    }

    @GET
    @Path("/{slug}/comments")
    @Secured(optional = true)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCommentsBySlug(
            @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
            @Context SecurityContext securityContext) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
        final var comments = findCommentsByArticleSlug.handle(slug);
        return Response.ok(noWrapObjectMapper.writeValueAsString(resourceUtils.commentsResponse(comments, loggedUserId)))
                .status(Response.Status.OK)
                .build();
    }

    @POST
    @Transactional
    @Path("/{slug}/comments")
    @Secured({Role.ADMIN, Role.USER})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createComment(
            @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
            @Valid NewCommentRequestWrapper newCommentRequest,
            @Context SecurityContext securityContext) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
        final var comment =
                createComment.handle(newCommentRequest.getComment().toNewCommentInput(loggedUserId, slug));
        return Response.ok(objectMapper.writeValueAsString(resourceUtils.commentResponse(comment, loggedUserId)))
                .status(Response.Status.OK)
                .build();
    }

    @DELETE
    @Transactional
    @Path("/{slug}/comments/{id}")
    @Secured({Role.ADMIN, Role.USER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteComment(
            @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
            @PathParam("id") @NotNull(message = ValidationMessages.COMMENT_ID_MUST_BE_NOT_NULL) UUID id,
            @Context SecurityContext securityContext) {
        final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
        deleteComment.handle(new DeleteCommentInput(id, loggedUserId, slug));
        return Response.ok().build();
    }

    @POST
    @Transactional
    @Path("/{slug}/favorite")
    @Secured({Role.ADMIN, Role.USER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response favoriteArticle(
            @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
            @Context SecurityContext securityContext) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
        favoriteArticle.handle(slug, loggedUserId);
        final var article = findArticleBySlug.handle(slug);
        return Response.ok(objectMapper.writeValueAsString(resourceUtils.articleResponse(article, loggedUserId)))
                .status(Response.Status.OK)
                .build();
    }

    @DELETE
    @Transactional
    @Path("/{slug}/favorite")
    @Secured({Role.ADMIN, Role.USER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response unfavoriteArticle(
            @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
            @Context SecurityContext securityContext) throws JsonProcessingException {
        final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
        unfavoriteArticle.handle(slug, loggedUserId);
        final var article = findArticleBySlug.handle(slug);
        return Response.ok(objectMapper.writeValueAsString(resourceUtils.articleResponse(article, loggedUserId)))
                .status(Response.Status.OK)
                .build();
    }
}
