package com.epam.exception;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(long id) {
        super(String.valueOf(id));
    }
}
