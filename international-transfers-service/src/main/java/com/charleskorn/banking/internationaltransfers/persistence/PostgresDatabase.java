package com.charleskorn.banking.internationaltransfers.persistence;

import com.charleskorn.banking.internationaltransfers.Transfer;

import java.math.BigDecimal;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostgresDatabase implements Database {
    private final String connectionUrl = "jdbc:postgresql://database/international-transfers-service" +
            "?user=international-transfers-service" +
            "&password=TheSuperSecretPassword" +
            "&ssl=false";

    @Override
    public Transfer saveTransfer(String fromCurrency, String toCurrency, OffsetDateTime transferDate, BigDecimal originalAmount, BigDecimal exchangeRate) throws SQLException {
        try (Connection conn = getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO transfers (id, from_currency, to_currency, transfer_date, original_amount, exchange_rate) VALUES(?, ?, ?, ?, ?, ?);")) {
                UUID id = UUID.randomUUID();

                st.setObject(1, id);
                st.setString(2, fromCurrency);
                st.setString(3, toCurrency);
                st.setObject(4, transferDate);
                st.setBigDecimal(5, originalAmount);
                st.setBigDecimal(6, exchangeRate);
                st.executeUpdate();

                return new Transfer(id, fromCurrency, toCurrency, transferDate, originalAmount, exchangeRate);
            }
        }
    }

    @Override
    public List<Transfer> getAllTransfers() throws SQLException {
        try (Connection conn = getConnection()) {
            try (Statement st = conn.createStatement()) {
                try (ResultSet results = st.executeQuery("SELECT id, from_currency, to_currency, transfer_date, original_amount, exchange_rate FROM transfers;")) {
                    List<Transfer> transfers = new ArrayList<>();

                    while (results.next()) {
                        UUID id = UUID.fromString(results.getString("id"));
                        String fromCurrency = results.getString("from_currency");
                        String toCurrency = results.getString("to_currency");
                        OffsetDateTime transferDate = results.getObject("transfer_date", OffsetDateTime.class);
                        BigDecimal originalAmount = results.getBigDecimal("original_amount");
                        BigDecimal exchangeRate = results.getBigDecimal("exchange_rate");

                        transfers.add(new Transfer(id, fromCurrency, toCurrency, transferDate, originalAmount, exchangeRate));
                    }

                    return transfers;
                }
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionUrl);
    }
}
