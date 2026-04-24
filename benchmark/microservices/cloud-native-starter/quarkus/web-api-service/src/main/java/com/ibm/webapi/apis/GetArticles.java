package com.ibm.webapi.apis;

import java.util.List;

import com.ibm.webapi.business.Article;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1")
public class GetArticles {

	@Inject
	WebApiService service;

	@GET
	@Path("/getmultiple")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getArticles() {
		List<Article> articles = service.getArticles();
		return Response.ok(articles).build();
	}
}
