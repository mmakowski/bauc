package com.mmakowski.bauc.api;

import com.google.common.base.Objects;

public final class Item {
    public final int id;
    // TODO: other item attributes

    public Item(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equal(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                '}';
    }
}
