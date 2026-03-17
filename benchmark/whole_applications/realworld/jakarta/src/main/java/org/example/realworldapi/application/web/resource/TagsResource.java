package org.example.realworldapi.application.web.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.realworldapi.application.web.model.response.TagsResponse;
import org.example.realworldapi.domain.feature.FindTags;
import org.example.realworldapi.domain.model.tag.Tag;

import java.util.List;

@Path("/tags")
public class TagsResource {

    @Inject
    private FindTags findTags;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTags() {
        List<Tag> tags = findTags.handle();
        return Response.ok(new TagsResponse(tags))
                .status(Response.Status.OK)
                .build();
    }
}
