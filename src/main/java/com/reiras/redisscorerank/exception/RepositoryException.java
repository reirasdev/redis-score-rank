package com.reiras.redisscorerank.exception;

public class RepositoryException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public RepositoryException(String msg) {
		super(msg);
	}

	public RepositoryException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
