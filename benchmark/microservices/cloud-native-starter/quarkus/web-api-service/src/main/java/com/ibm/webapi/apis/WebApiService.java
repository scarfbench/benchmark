package com.ibm.webapi.apis;

import java.util.ArrayList;
import java.util.List;

import com.ibm.webapi.business.Article;
import com.ibm.webapi.business.Author;
import com.ibm.webapi.business.CoreArticle;
import com.ibm.webapi.data.ArticlesServiceDataAccess;
import com.ibm.webapi.data.AuthorsServiceDataAccess;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WebApiService {

	@Inject
	ArticlesServiceDataAccess articlesDataAccess;

	@Inject
	AuthorsServiceDataAccess authorsDataAccess;

	public List<Article> getArticles() {
		List<Article> articles = new ArrayList<>();
		List<CoreArticle> coreArticles = articlesDataAccess.getArticles(5);

		for (CoreArticle coreArticle : coreArticles) {
			Article article = new Article();
			article.id = coreArticle.id;
			article.title = coreArticle.title;
			article.url = coreArticle.url;
			article.authorName = coreArticle.author;
			Author author = authorsDataAccess.getAuthor(coreArticle.author);
			if (author != null) {
				article.authorBlog = author.blog;
				article.authorTwitter = author.twitter;
			} else {
				article.authorBlog = "";
				article.authorTwitter = "";
			}
			articles.add(article);
		}
		return articles;
	}
}
