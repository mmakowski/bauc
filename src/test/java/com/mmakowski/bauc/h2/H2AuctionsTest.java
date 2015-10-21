package com.mmakowski.bauc.h2;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mmakowski.bauc.api.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public final class H2AuctionsTest {
    // item's bids

    @Test
    public void thereAreNoBidsOnNewItem() {
        final H2Auctions sut = new H2Auctions();
        final Item item = sut.listItem(new ItemRegistration());

        Assert.assertEquals(ImmutableList.of(), sut.allBidsForItem(item.id));
    }

    @Test
    public void bidsFromMultipleUsers() {
        final List<Bid> allBids = ImmutableList.of(
                new Bid(1, 1, 3),
                new Bid(1, 2, 4)
        );

        verifyAllBidsForItem(1, 2, allBids, 1, allBids);
    }

    @Test
    public void multipleBidsFromASingleUser() {
        final List<Bid> allBids = ImmutableList.of(
                new Bid(1, 1, 3),
                new Bid(1, 1, 5)
        );

        verifyAllBidsForItem(1, 1, allBids, 1, allBids);
    }

    @Test
    public void bidsOnOtherItemsAreExcluded() {
        final Bid item1Bid = new Bid(1, 1, 5);
        final Bid item2Bid = new Bid(2, 1, 5);
        final List<Bid> allBids = ImmutableList.of(
                item1Bid,
                item2Bid
        );

        verifyAllBidsForItem(2, 1, allBids, 1, ImmutableList.of(item1Bid));
    }

    private static void verifyAllBidsForItem(final int itemCount,
                                             final int userCount,
                                             final Iterable<Bid> bids,
                                             final int itemId,
                                             final Iterable<Bid> expectedItemBids) {
        final H2Auctions sut = new H2Auctions();
        for (int i = 0; i < itemCount; i++) sut.listItem(new ItemRegistration());
        for (int i = 0; i < userCount; i++) sut.addUser(new UserRegistration());
        bids.forEach((bid) -> {
            try {
                sut.recordBid(bid);
            } catch (BidException e) {
                throw new RuntimeException(e);
            }
        });

        Assert.assertEquals(expectedItemBids, sut.allBidsForItem(itemId));
    }

    // winning bid

    @Test
    public void winningBidIsNotDefinedWhenThereAreNoBidsOnAnItem() {
        final H2Auctions sut = new H2Auctions();
        final Item item = sut.listItem(new ItemRegistration());

        Assert.assertEquals(Optional.empty(), sut.winningBid(item.id));
    }

    @Test
    public void onlyBidIsTheWinningBid() throws BidException {
        final H2Auctions sut = new H2Auctions();
        final Item item = sut.listItem(new ItemRegistration());
        final User user = sut.addUser(new UserRegistration());
        final Bid onlyBid = new Bid(item.id, user.id, 3);
        sut.recordBid(onlyBid);

        Assert.assertEquals(Optional.of(onlyBid), sut.winningBid(item.id));
    }

    @Test
    public void highestBidWins() throws BidException {
        final H2Auctions sut = twoUsersBidOnTheSameItem(4, 5);

        Assert.assertEquals(Optional.of(new Bid(1, 2, 5)), sut.winningBid(1));
    }

    @Test(expected=BidTooLowException.class)
    public void bidIsRejectedIfHigherBidAlreadyEntered() throws BidException {
        twoUsersBidOnTheSameItem(5, 4);
    }

    @Test(expected=BidTooLowException.class)
    public void bidIsRejectedIfEqualBidAlreadyEntered() throws BidException {
        twoUsersBidOnTheSameItem(5, 5);
    }

    private static H2Auctions twoUsersBidOnTheSameItem(final int firstAmount, final int secondAmount) throws BidException {
        final H2Auctions sut = new H2Auctions();
        final Item item = sut.listItem(new ItemRegistration());
        final User user1 = sut.addUser(new UserRegistration());
        final User user2 = sut.addUser(new UserRegistration());
        sut.recordBid(new Bid(item.id, user1.id, firstAmount));
        sut.recordBid(new Bid(item.id, user2.id, secondAmount));
        return sut;
    }

    // user's items

    @Test
    public void thereAreNoItemsIfUserDidNotBid() {
        final H2Auctions sut = new H2Auctions();
        final User user = sut.addUser(new UserRegistration());

        Assert.assertEquals(ImmutableList.of(), sut.allItemsForUser(user.id));
    }

    @Test
    public void itemIsOnlyListedOnceEvenIfUserBidManyTimes() throws BidException {
        final H2Auctions sut = new H2Auctions();
        final Item item = sut.listItem(new ItemRegistration());
        final User user = sut.addUser(new UserRegistration());
        sut.recordBid(new Bid(item.id, user.id, 1));
        sut.recordBid(new Bid(item.id, user.id, 2));

        Assert.assertEquals(ImmutableList.of(item), sut.allItemsForUser(user.id));
    }

    @Test
    public void allItemsUserBidOnAreListed() throws BidException {
        final H2Auctions sut = new H2Auctions();
        final Item item1 = sut.listItem(new ItemRegistration());
        final Item item2 = sut.listItem(new ItemRegistration());
        final User user = sut.addUser(new UserRegistration());
        sut.recordBid(new Bid(item1.id, user.id, 1));
        sut.recordBid(new Bid(item2.id, user.id, 1));

        Assert.assertEquals(ImmutableSet.of(item1, item2), ImmutableSet.copyOf(sut.allItemsForUser(user.id)));
    }

    @Test
    public void itemsOtherUsersBidOnAreNotListed() throws BidException {
        final H2Auctions sut = new H2Auctions();
        final Item item1 = sut.listItem(new ItemRegistration());
        final Item item2 = sut.listItem(new ItemRegistration());
        final User user1 = sut.addUser(new UserRegistration());
        final User user2 = sut.addUser(new UserRegistration());
        sut.recordBid(new Bid(item1.id, user1.id, 1));
        sut.recordBid(new Bid(item2.id, user2.id, 1));

        Assert.assertEquals(ImmutableList.of(item1), sut.allItemsForUser(user1.id));
    }
}