package com.mmakowski.bauc.api;

public interface Auctions {
    void listItem(Item item);

    void recordBid(Bid bid);

    Bid winningBid(int itemId);

    Iterable<Bid> allBidsForItem(int itemId);

    Iterable<Item> allItemsForUser(int userId);
}
