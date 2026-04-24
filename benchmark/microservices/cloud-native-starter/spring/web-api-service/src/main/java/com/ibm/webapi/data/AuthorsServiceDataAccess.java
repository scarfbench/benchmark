package com.ibm.webapi.data;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.ibm.webapi.business.Author;
import com.ibm.webapi.business.NonexistentAuthor;

@Component
public class AuthorsServiceDataAccess {

	@Value("${authors.service.url:http://authors-service:9080/api/v1/}")
	private String baseUrl;

	private final RestTemplate restTemplate;

	public AuthorsServiceDataAccess(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public Author getAuthor(String name) throws NoConnectivity, NonexistentAuthor {
		try {
			String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
			URI uri = URI.create(baseUrl + "getauthor?name=" + encodedName);
			return restTemplate.getForObject(uri, Author.class);
		} catch (RestClientException e) {
			throw new NoConnectivity(e);
		} catch (Exception e) {
			throw new NoConnectivity(e);
		}
	}
}
