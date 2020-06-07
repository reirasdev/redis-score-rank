package com.reiras.redisscorerank.exception;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);

	@ExceptionHandler(RepositoryException.class)
	public ResponseEntity<StandardError> repositoryException(RepositoryException exception,
			HttpServletRequest request) {

		StandardError err = new StandardError(System.currentTimeMillis(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Error handling database operation", exception.getMessage(), request.getRequestURI());

		LOGGER.error(err.toString(), exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<StandardError> methodArgumentNotValid(MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		ValidationError err = new ValidationError(System.currentTimeMillis(), HttpStatus.BAD_REQUEST.value(),
				"Field validation error", exception.getMessage(), request.getRequestURI());

		for (FieldError x : exception.getBindingResult().getFieldErrors()) {
			err.AddError(x.getField(), x.getDefaultMessage());
		}

		//This error is not logged for not unnecessarily increasing log every time a user sends an invalid parameter
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
	}

}
