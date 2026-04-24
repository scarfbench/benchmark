package com.ibm.articles;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/")
public class ArticlesResource {

	@Inject
	InMemoryDataStore dataStore;

	@POST
	@Path("/create")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addArticle(Article newArticle) {
		if (newArticle == null || newArticle.title == null || newArticle.title.isEmpty()) {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
		Article article = dataStore.addArticle(newArticle);
		return Response.status(Response.Status.CREATED).entity(article).build();
	}

	@GET
	@Path("/getone")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getArticle(@QueryParam("id") String id) {
		Article article = dataStore.getArticle(id);
		if (article == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		return Response.ok(article).build();
	}

	@GET
	@Path("/getmultiple")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getArticles(@QueryParam("amount") int amount) {
		if (amount < 0) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		List<Article> articles = dataStore.getArticles(amount);
		return Response.ok(articles).build();
	}
}
