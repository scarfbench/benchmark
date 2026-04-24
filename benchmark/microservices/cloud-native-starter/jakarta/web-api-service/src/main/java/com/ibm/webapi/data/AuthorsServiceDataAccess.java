package com.ibm.webapi.data;

import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.ibm.webapi.business.Author;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@ApplicationScoped
public class AuthorsServiceDataAccess {

	@Inject
	@ConfigProperty(name = "authors.service.url")
	String baseUrl;

	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.build();

	public Author getAuthor(String name) throws NoConnectivity {
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
			try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
				JsonObject o = reader.readObject();
				Author a = new Author();
				a.name = o.containsKey("name") && !o.isNull("name") ? o.getString("name") : name;
				a.twitter = o.containsKey("twitter") && !o.isNull("twitter") ? o.getString("twitter") : "";
				a.blog = o.containsKey("blog") && !o.isNull("blog") ? o.getString("blog") : "";
				return a;
			}
		} catch (Exception e) {
			throw new NoConnectivity(e);
		}
	}
}
