package com.example.exception;

public class InvalidAuctionStateException extends RuntimeException {

    public InvalidAuctionStateException(String message) {
        super(message);
    }
}
