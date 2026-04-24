package com.ibm.webapi.apis;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.webapi.business.Article;
import com.ibm.webapi.business.CoreArticle;
import com.ibm.webapi.business.InvalidArticle;
import com.ibm.webapi.business.NoDataAccess;

@RestController
@RequestMapping("/web-api/v1")
public class WebApiController {

	private final WebApiService service;

	public WebApiController(WebApiService service) {
		this.service = service;
	}

	@GetMapping(value = "/getmultiple", produces = "application/json")
	public ResponseEntity<List<Article>> getArticles() {
		try {
			return ResponseEntity.ok(service.getArticles());
		} catch (NoDataAccess e) {
			return ResponseEntity.ok(service.fallbackNoArticlesService());
		}
	}

	@PostMapping(value = "/create", consumes = "application/json", produces = "application/json")
	public ResponseEntity<CoreArticle> createArticle(@RequestBody CoreArticle newArticle) {
		try {
			CoreArticle article = service.addArticle(newArticle.title, newArticle.url, newArticle.author);
			return ResponseEntity.status(HttpStatus.CREATED).body(article);
		} catch (InvalidArticle e) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		} catch (NoDataAccess e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleAny(Exception e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
	}
}
