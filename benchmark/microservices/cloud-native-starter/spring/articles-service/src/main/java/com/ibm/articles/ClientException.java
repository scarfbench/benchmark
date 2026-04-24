package com.ibm.articles;

public class ClientException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ClientException() {
	}

	public ClientException(String message) {
		super(message);
	}
}
