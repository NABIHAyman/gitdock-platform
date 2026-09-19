package edu.ehei.gitdock.gitdockproject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Cette annotation dit à Spring de renvoyer automatiquement une erreur HTTP 404
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SagaExecutionException extends RuntimeException {
    public SagaExecutionException(String message) {
        super(message);
    }
}