package com.academia.auth.Exceptions;

public class ResourceNotFound extends RuntimeException {
    
    public ResourceNotFound(String msg) {
        super(msg);
    }
}
