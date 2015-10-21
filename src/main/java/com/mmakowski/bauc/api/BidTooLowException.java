package com.mmakowski.bauc.api;

public final class BidTooLowException extends BidException {
    final int minimumAllowedBid;

    public BidTooLowException(Bid bid, int minimumAllowedBid) {
        super(bid, "the bid of " + bid.amount + " was too low; minimum allowed bid: " + minimumAllowedBid);
        this.minimumAllowedBid = minimumAllowedBid;
    }
}
