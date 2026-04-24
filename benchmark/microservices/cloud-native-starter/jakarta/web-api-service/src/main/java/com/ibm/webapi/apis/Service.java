package com.ibm.webapi.apis;

import java.util.ArrayList;
import java.util.List;

import com.ibm.webapi.business.Article;
import com.ibm.webapi.business.Author;
import com.ibm.webapi.business.CoreArticle;
import com.ibm.webapi.business.NoDataAccess;
import com.ibm.webapi.data.ArticlesServiceDataAccess;
import com.ibm.webapi.data.AuthorsServiceDataAccess;
import com.ibm.webapi.data.NoConnectivity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class Service {

	@Inject
	ArticlesServiceDataAccess articlesDataAccess;

	@Inject
	AuthorsServiceDataAccess authorsDataAccess;

	private List<Article> lastReadArticles;

	public List<Article> getArticles() throws NoDataAccess {
		List<Article> articles = new ArrayList<>();
		List<CoreArticle> coreArticles;
		int requestedAmount = 5;

		try {
			coreArticles = articlesDataAccess.getArticles(requestedAmount);
		} catch (NoConnectivity e) {
			throw new NoDataAccess(e);
		}

		for (CoreArticle coreArticle : coreArticles) {
			Article article = new Article();
			article.id = coreArticle.id;
			article.title = coreArticle.title;
			article.url = coreArticle.url;
			article.authorName = coreArticle.author;
			try {
				Author author = authorsDataAccess.getAuthor(coreArticle.author);
				if (author != null) {
					article.authorBlog = author.blog;
					article.authorTwitter = author.twitter;
				} else {
					article.authorBlog = "";
					article.authorTwitter = "";
				}
			} catch (Exception e) {
				article.authorBlog = "";
				article.authorTwitter = "";
			}
			articles.add(article);
		}
		lastReadArticles = articles;
		return articles;
	}

	public List<Article> fallbackNoArticlesService() {
		if (lastReadArticles == null) lastReadArticles = new ArrayList<>();
		return lastReadArticles;
	}
}
