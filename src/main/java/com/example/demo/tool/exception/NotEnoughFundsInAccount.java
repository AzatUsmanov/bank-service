package com.example.demo.tool.exception;

public class NotEnoughFundsInAccount extends Exception {

    public NotEnoughFundsInAccount() {
        super();
    }

    public NotEnoughFundsInAccount(String message) {
        super(message);
    }

}
