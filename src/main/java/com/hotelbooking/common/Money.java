package com.hotelbooking.common;


import com.hotelbooking.common.exception.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

    public static final Currency INR = Currency.getInstance("INR");

    public Money {
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(currency, "currency is required");
        if (amount.signum() < 0) {
            throw new ValidationException("Amount cannot be negative: " + amount);
        }
        // Normalise scale so 1000 and 1000.00 are equal.
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    // ---- factories ----

    public static Money inr(BigDecimal amount) {
        return new Money(amount, INR);
    }

    public static Money inr(String amount) {
        return inr(new BigDecimal(amount));
    }

    public static Money zeroInr() {
        return inr(BigDecimal.ZERO);
    }

    // ---- arithmetic ----

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money multiply(long factor) {
        if (factor < 0) {
            throw new ValidationException("Multiplier cannot be negative");
        }
        return new Money(amount.multiply(BigDecimal.valueOf(factor)), currency);
    }

    public Money percentage(int percent) {
        if (percent < 0 || percent > 100) {
            throw new ValidationException("Percentage must be between 0 and 100");
        }
        return new Money(
                amount.multiply(BigDecimal.valueOf(percent))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP),
                currency);
    }

    // ---- predicates ----

    /** True when this amount lies within the optional [min, max] bounds. Null means "no bound". */
    public boolean isWithin(Money min, Money max) {
        if (min != null && compareTo(min) < 0) return false;
        return max == null || compareTo(max) <= 0;
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount);
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new ValidationException(
                    "Cannot combine " + currency + " and " + other.currency);
        }
    }

    @Override
    public String toString() {
        return currency.getCurrencyCode() + " " + amount;
    }
}
