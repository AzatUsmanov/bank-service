package com.example.demo.tool.exception;

public class TransferToNonExistentAccountException extends Exception {

    public TransferToNonExistentAccountException(String message) {
        super(message);
    }

    public TransferToNonExistentAccountException() {
        super();
    }
}
