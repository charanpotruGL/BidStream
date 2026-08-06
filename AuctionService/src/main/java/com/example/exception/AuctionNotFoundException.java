package com.example.exception;

public class AuctionNotFoundException extends RuntimeException {

    public AuctionNotFoundException(String message) {
        super(message);
    }

    public AuctionNotFoundException(Long id) {
        super("Auction not found with id: " + id);
    }
}
