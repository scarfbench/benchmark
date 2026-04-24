package com.ibm.authors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/getauthor")
public class GetAuthor {

	@Inject
	AuthorsStore store;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAuthor(@QueryParam("name") String name) {
		Author author = store.findByName(name);
		if (author == null) {
			// Fallback: return a default author so web-api can still enrich articles.
			author = new Author(name == null ? "Unknown" : name,
			                    "https://twitter.com/unknown",
			                    "https://example.com/unknown");
		}
		return Response.ok(author).build();
	}
}
