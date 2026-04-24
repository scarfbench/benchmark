package com.ibm.articles;

import java.io.Serializable;

public class Article implements Serializable {
	private static final long serialVersionUID = 1L;

	public String id;
	public String title;
	public String url;
	public String author;
	public String creationDate;

	public Article() {
	}

	public Article(String id, String title, String url, String author, String creationDate) {
		this.id = id;
		this.title = title;
		this.url = url;
		this.author = author;
		this.creationDate = creationDate;
	}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }
	public String getAuthor() { return author; }
	public void setAuthor(String author) { this.author = author; }
	public String getCreationDate() { return creationDate; }
	public void setCreationDate(String creationDate) { this.creationDate = creationDate; }
}
