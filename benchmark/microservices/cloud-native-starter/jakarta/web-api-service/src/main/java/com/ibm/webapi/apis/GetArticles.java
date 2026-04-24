package com.ibm.webapi.apis;

import java.util.List;

import com.ibm.webapi.business.Article;
import com.ibm.webapi.business.NoDataAccess;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/")
public class GetArticles {

	@Inject
	Service service;

	@GET
	@Path("/getmultiple")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getArticles() {
		try {
			List<Article> articles = service.getArticles();
			return Response.ok(articles).build();
		} catch (NoDataAccess e) {
			return Response.ok(service.fallbackNoArticlesService()).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
