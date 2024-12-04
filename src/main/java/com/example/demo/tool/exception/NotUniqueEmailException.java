package com.example.demo.tool.exception;

public class NotUniqueEmailException extends Exception {

    public NotUniqueEmailException() {
        super();
    }

    public NotUniqueEmailException(String message) {
        super(message);
    }

}
