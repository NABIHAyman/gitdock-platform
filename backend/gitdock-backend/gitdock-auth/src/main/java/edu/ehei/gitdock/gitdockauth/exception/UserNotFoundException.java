package edu.ehei.gitdock.gitdockauth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UserNotFoundException extends UsernameNotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }
}