package com.mmakowski.bauc.api;

import java.util.Optional;

public interface Auctions {
    User addUser(UserRegistration user);

    Item listItem(ItemRegistration item);

    void recordBid(Bid bid) throws BidException;

    Optional<Bid> winningBid(int itemId);

    Iterable<Bid> allBidsForItem(int itemId);

    Iterable<Item> allItemsForUser(int userId);
}
