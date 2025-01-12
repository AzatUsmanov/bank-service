package com.example.demo.tool.exception;

public class NotEnoughFundsInAccountException extends Exception {

    public NotEnoughFundsInAccountException() {
        super();
    }

    public NotEnoughFundsInAccountException(String message) {
        super(message);
    }

}
