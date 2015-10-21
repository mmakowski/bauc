package com.mmakowski.bauc.h2;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mmakowski.bauc.api.*;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public final class H2Auctions implements Auctions {
    private final Connection connection;

    public H2Auctions() {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:mem:");
            connection.setAutoCommit(true);
            initialiseSchema();
        } catch (final ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User addUser(final UserRegistration registration) {
        return sqlExceptionToRuntimeException(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "insert into users () values ()")) {
                stmt.executeUpdate();
                return new User(soleGeneratedKey(stmt));
            }
        });
    }

    @Override
    public Item listItem(final ItemRegistration registration) {
        return sqlExceptionToRuntimeException(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "insert into items () values ()")) {
                stmt.executeUpdate();
                return new Item(soleGeneratedKey(stmt));
            }
        });
    }

    @Override
    public void recordBid(final Bid bid) throws BidException {
        ensureIsHigherThanCurrentWinning(bid);
        sqlExceptionToRuntimeException(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "insert into bids (item_id, user_id, amount) values (?, ?, ?)")) {
                stmt.setInt(1, bid.itemId);
                stmt.setInt(2, bid.userId);
                stmt.setInt(3, bid.amount);
                stmt.execute();
                return null;
            }
        });
    }

    private void ensureIsHigherThanCurrentWinning(final Bid bid) throws BidTooLowException {
        final Optional<Bid> maybeWinningBid = winningBid(bid.itemId);
        if (maybeWinningBid.isPresent()) {
            final Bid winningBid = maybeWinningBid.get();
            if (winningBid.amount >= bid.amount)
                throw new BidTooLowException(bid, winningBid.amount + 1);
        }
    }

    @Override
    public Optional<Bid> winningBid(final int itemId) {
        return sqlExceptionToRuntimeException(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "select user_id, amount from bids where item_id = ? order by amount desc limit 1")) {
                stmt.setInt(1, itemId);
                try (ResultSet result = stmt.executeQuery()) {
                    if (result.next())
                        return Optional.of(new Bid(itemId, result.getInt("user_id"), result.getInt("amount")));
                    else
                        return Optional.<Bid>empty();
                }
            }
        });
    }

    @Override
    public Iterable<Bid> allBidsForItem(final int itemId) {
        return sqlExceptionToRuntimeException(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "select user_id, amount from bids where item_id = ? order by amount")) {
                stmt.setInt(1, itemId);
                try (ResultSet result = stmt.executeQuery()) {
                    final ImmutableList.Builder<Bid> builder = ImmutableList.builder();
                    while (result.next())
                        builder.add(new Bid(itemId, result.getInt("user_id"), result.getInt("amount")));
                    return builder.build();
                }
            }
        });
    }

    @Override
    public Iterable<Item> allItemsForUser(final int userId) {
        return sqlExceptionToRuntimeException(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "select distinct item_id from bids where user_id = ? order by item_id")) {
                stmt.setInt(1, userId);
                try (ResultSet result = stmt.executeQuery()) {
                    final ImmutableList.Builder<Item> builder = ImmutableList.builder();
                    while (result.next())
                        builder.add(new Item(result.getInt("item_id")));
                    return builder.build();
                }
            }
        });
    }

    private void initialiseSchema() throws SQLException {
        for (final String ddl : DDL) {
            try (PreparedStatement statement = connection.prepareStatement(ddl)) {
                statement.execute();
            }
        }
    }

    private static int soleGeneratedKey(final PreparedStatement stmt) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            Preconditions.checkState(generatedKeys.next(), "no id was generated");
            final int key = generatedKeys.getInt(1);
            Preconditions.checkState(!generatedKeys.next(), "more than one key was generated");
            return key;
        }
    }

    private static final List<String> DDL = ImmutableList.of(
            "create table items (" +
            "  item_id int auto_increment not null primary key" +
            ")",

            "create table users (" +
            "  user_id int auto_increment not null primary key" +
            ")",

            "create table bids (" +
            "  item_id int not null," +
            "  user_id int not null," +
            "  amount int not null," +
            "  constraint bids_fk_items foreign key (item_id) references items (item_id)," +
            "  constraint bids_fk_users foreign key (user_id) references users (user_id)," +
            ")"
    );

    private interface DatabaseOperation<T> {
        T execute() throws SQLException;
    }

    private static <T> T sqlExceptionToRuntimeException(final DatabaseOperation<T> operation) {
        try {
            return operation.execute();
        } catch (final SQLException e) {
            throw new RuntimeException("error in database operation", e);
        }
    }
}
