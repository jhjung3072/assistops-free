package com.assistops.api.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
	Instant timestamp,
	int status,
	String error,
	String message,
	Map<String, String> validationErrors
) {

	public static ErrorResponse of(HttpStatus status, String message) {
		return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, null);
	}

	public static ErrorResponse validation(Map<String, String> validationErrors) {
		return new ErrorResponse(
			Instant.now(),
			HttpStatus.BAD_REQUEST.value(),
			HttpStatus.BAD_REQUEST.getReasonPhrase(),
			"Request validation failed.",
			validationErrors
		);
	}
}
