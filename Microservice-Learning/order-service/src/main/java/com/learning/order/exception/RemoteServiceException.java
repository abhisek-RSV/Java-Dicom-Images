package com.learning.order.exception;

import lombok.Getter;

/** Another service answered with an error (e.g. book-service returned 404). */
@Getter
public class RemoteServiceException extends RuntimeException {

    private final int status;

    public RemoteServiceException(int status, String message) {
        super(message);
        this.status = status;
    }
}
