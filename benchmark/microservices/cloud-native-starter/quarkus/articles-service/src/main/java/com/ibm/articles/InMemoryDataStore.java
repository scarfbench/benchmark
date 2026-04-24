package com.ibm.articles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InMemoryDataStore {

	private final Map<String, Article> articles = new ConcurrentHashMap<>();

	@PostConstruct
	public void init() {
		addSample("Cloud Native Spring Boot", "https://ibm.com", "Billy Korando");
		sleep();
		addSample("The Avengers", "https://ibm.com", "Niklas Heidloff");
		sleep();
		addSample("Developing and debugging Microservices with Java",
		          "http://heidloff.net/article/debugging-microservices-java-kubernetes",
		          "Niklas Heidloff");
	}

	public Article addArticle(Article article) {
		long id = new java.util.Date().getTime();
		String idAsString = String.valueOf(id);
		article.creationDate = idAsString;
		if (article.id == null || article.id.isEmpty()) {
			article.id = idAsString.substring(6);
		}
		articles.put(article.id, article);
		return article;
	}

	public Article getArticle(String id) {
		return articles.get(id);
	}

	public List<Article> getArticles(int amount) {
		List<Article> all = new ArrayList<>(articles.values());
		Comparator<Article> comparator = (left, right) -> {
			try {
				int l = Integer.parseInt(left.creationDate.substring(6));
				int r = Integer.parseInt(right.creationDate.substring(6));
				return r - l;
			} catch (Exception e) {
				return 0;
			}
		};
		Collections.sort(all, comparator);
		if (amount > 0 && all.size() > amount) {
			return new ArrayList<>(all.subList(0, amount));
		}
		return all;
	}

	private void addSample(String title, String url, String author) {
		Article article = new Article();
		article.title = title;
		article.url = url;
		article.author = author;
		addArticle(article);
	}

	private void sleep() {
		try {
			Thread.sleep(5);
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}
	}
}
