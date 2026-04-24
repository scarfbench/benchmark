package com.ibm.webapi.data;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.webapi.business.Author;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthorsServiceDataAccess {

	@Inject
	@ConfigProperty(name = "authors.service.url")
	String baseUrl;

	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.build();

	private final ObjectMapper mapper = new ObjectMapper();

	public Author getAuthor(String name) {
		try {
			String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(baseUrl + "getauthor?name=" + encoded))
					.timeout(Duration.ofSeconds(10))
					.GET()
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() >= 400) {
				return null;
			}
			return mapper.readValue(response.body(), Author.class);
		} catch (Exception e) {
			return null;
		}
	}
}
