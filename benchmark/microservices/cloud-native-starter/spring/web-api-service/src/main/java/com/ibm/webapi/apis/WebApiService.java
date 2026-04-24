package com.ibm.webapi.apis;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ibm.webapi.business.Article;
import com.ibm.webapi.business.CoreArticle;
import com.ibm.webapi.business.InvalidArticle;
import com.ibm.webapi.business.NoDataAccess;
import com.ibm.webapi.business.NonexistentAuthor;
import com.ibm.webapi.data.ArticlesServiceDataAccess;
import com.ibm.webapi.data.AuthorsServiceDataAccess;
import com.ibm.webapi.data.NoConnectivity;

@Service
public class WebApiService {

	private final ArticlesServiceDataAccess articlesDataAccess;
	private final AuthorsServiceDataAccess authorsDataAccess;

	private List<Article> lastReadArticles;

	public WebApiService(ArticlesServiceDataAccess articlesDataAccess,
	                     AuthorsServiceDataAccess authorsDataAccess) {
		this.articlesDataAccess = articlesDataAccess;
		this.authorsDataAccess = authorsDataAccess;
	}

	public CoreArticle addArticle(String title, String url, String author) throws NoDataAccess, InvalidArticle {
		if (title == null) {
			throw new InvalidArticle();
		}
		long id = new java.util.Date().getTime();
		String idAsString = String.valueOf(id);

		if (url == null) url = "Unknown";
		if (author == null) author = "Unknown";

		CoreArticle article = new CoreArticle();
		article.title = title;
		article.id = idAsString;
		article.url = url;
		article.author = author;

		try {
			articlesDataAccess.addArticle(article);
			return article;
		} catch (NoConnectivity e) {
			throw new NoDataAccess(e);
		}
	}

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
				com.ibm.webapi.business.Author author = authorsDataAccess.getAuthor(coreArticle.author);
				if (author != null) {
					article.authorBlog = author.blog;
					article.authorTwitter = author.twitter;
				} else {
					article.authorBlog = "";
					article.authorTwitter = "";
				}
			} catch (NoConnectivity | NonexistentAuthor e) {
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
