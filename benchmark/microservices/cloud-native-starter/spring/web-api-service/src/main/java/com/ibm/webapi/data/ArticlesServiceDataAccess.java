package com.ibm.webapi.data;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.ibm.webapi.business.CoreArticle;
import com.ibm.webapi.business.InvalidArticle;

@Component
public class ArticlesServiceDataAccess {

	@Value("${articles.service.url:http://articles-service:9080/articles/v1/}")
	private String baseUrl;

	private final RestTemplate restTemplate;

	public ArticlesServiceDataAccess(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public List<CoreArticle> getArticles(int amount) throws NoConnectivity {
		try {
			URI uri = URI.create(baseUrl + "getmultiple?amount=" + amount);
			CoreArticle[] articles = restTemplate.getForObject(uri, CoreArticle[].class);
			if (articles == null) {
				return java.util.Collections.emptyList();
			}
			return Arrays.asList(articles);
		} catch (RestClientException e) {
			throw new NoConnectivity(e);
		} catch (Exception e) {
			throw new NoConnectivity(e);
		}
	}

	public CoreArticle addArticle(CoreArticle article) throws NoConnectivity, InvalidArticle {
		try {
			URI uri = URI.create(baseUrl + "create");
			return restTemplate.postForObject(uri, article, CoreArticle.class);
		} catch (RestClientException e) {
			throw new NoConnectivity(e);
		} catch (Exception e) {
			throw new NoConnectivity(e);
		}
	}
}
