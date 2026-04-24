package com.ibm.authors;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuthorsStore {

	private final Map<String, Author> byName = new HashMap<>();

	@PostConstruct
	public void init() {
		add(new Author("Niklas Heidloff", "https://twitter.com/nheidloff", "http://heidloff.net"));
		add(new Author("Billy Korando", "@BillyKorando", "https://billykorando.com"));
		add(new Author("Harald Uebele", "@harald_u", "https://haralduebele.blog"));
	}

	private void add(Author author) {
		byName.put(author.name, author);
	}

	public Author findByName(String name) {
		return byName.get(name);
	}
}
