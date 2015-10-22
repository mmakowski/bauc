package com.mmakowski.bauc.api;

public final class BidTooLowException extends Exception {
    final Bid bid;
    final int minimumAllowedBid;

    public BidTooLowException(Bid bid, int minimumAllowedBid) {
        super("the bid of " + bid.amount + " was too low; minimum allowed bid: " + minimumAllowedBid);
        this.bid = bid;
        this.minimumAllowedBid = minimumAllowedBid;
    }
}
