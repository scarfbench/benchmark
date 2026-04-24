package com.ibm.authors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AuthorController {

	private AuthorService service;

	public AuthorController(AuthorService service) {
		this.service = service;
	}

	@GetMapping(value = "/getauthor", params = "name", produces = "application/json")
	public ResponseEntity<Author> getAuthor(@RequestParam(name = "name", required = true) String name) {
		return ResponseEntity.ok(service.getAuthorByName(name));
	}

	@ExceptionHandler(ClientException.class)
	public ResponseEntity<String> handleClientExceptions(ClientException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	}
}
