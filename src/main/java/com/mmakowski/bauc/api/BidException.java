package com.mmakowski.bauc.api;

public class BidException extends Exception {
    final Bid bid;

    BidException(final Bid bid, final String message) {
        super(message);
        this.bid = bid;
    }
}
