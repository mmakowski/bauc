package com.mmakowski.bauc.api;

import com.google.common.base.Objects;

public final class Bid {
    public final int itemId;
    public final int userId;
    public final int amount;

    public Bid(int itemId, int userId, int amount) {
        this.itemId = itemId;
        this.userId = userId;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bid bid = (Bid) o;
        return Objects.equal(itemId, bid.itemId) &&
                Objects.equal(userId, bid.userId) &&
                Objects.equal(amount, bid.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(itemId, userId, amount);
    }

    @Override
    public String toString() {
        return "Bid{" +
                "itemId=" + itemId +
                ", userId=" + userId +
                ", amount=" + amount +
                '}';
    }
}
