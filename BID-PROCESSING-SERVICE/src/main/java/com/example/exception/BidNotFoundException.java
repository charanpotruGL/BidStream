package com.example.exception;

public class BidNotFoundException extends RuntimeException {

    public BidNotFoundException(String message) {
        super(message);
    }

    public BidNotFoundException(Long id) {
        super("Bid not found with id: " + id);
    }
}
