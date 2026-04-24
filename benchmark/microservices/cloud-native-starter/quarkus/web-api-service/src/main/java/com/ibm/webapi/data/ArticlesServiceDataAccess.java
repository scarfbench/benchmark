package com.ibm.webapi.data;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.webapi.business.CoreArticle;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ArticlesServiceDataAccess {

	@Inject
	@ConfigProperty(name = "articles.service.url")
	String baseUrl;

	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.build();

	private final ObjectMapper mapper = new ObjectMapper();

	public List<CoreArticle> getArticles(int amount) {
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(baseUrl + "getmultiple?amount=" + amount))
					.timeout(Duration.ofSeconds(10))
					.GET()
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() >= 400) {
				return Collections.emptyList();
			}
			CoreArticle[] arr = mapper.readValue(response.body(), CoreArticle[].class);
			return Arrays.asList(arr);
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}
}
