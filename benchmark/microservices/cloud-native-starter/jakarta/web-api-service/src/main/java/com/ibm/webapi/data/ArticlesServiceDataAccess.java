package com.ibm.webapi.data;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.ibm.webapi.business.CoreArticle;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

@ApplicationScoped
public class ArticlesServiceDataAccess {

	@Inject
	@ConfigProperty(name = "articles.service.url")
	String baseUrl;

	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.build();

	public List<CoreArticle> getArticles(int amount) throws NoConnectivity {
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(baseUrl + "getmultiple?amount=" + amount))
					.timeout(Duration.ofSeconds(10))
					.GET()
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() >= 400) {
				throw new NoConnectivity();
			}
			List<CoreArticle> out = new ArrayList<>();
			try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
				JsonArray arr = reader.readArray();
				for (JsonValue v : arr) {
					if (v.getValueType() == JsonValue.ValueType.OBJECT) {
						JsonObject o = (JsonObject) v;
						CoreArticle a = new CoreArticle();
						a.id = o.containsKey("id") && !o.isNull("id") ? o.getString("id") : "";
						a.title = o.containsKey("title") && !o.isNull("title") ? o.getString("title") : "";
						a.url = o.containsKey("url") && !o.isNull("url") ? o.getString("url") : "";
						a.author = o.containsKey("author") && !o.isNull("author") ? o.getString("author") : "";
						out.add(a);
					}
				}
			}
			return out;
		} catch (Exception e) {
			throw new NoConnectivity(e);
		}
	}
}
