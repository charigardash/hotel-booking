package com.hotelbooking.common;

import com.hotelbooking.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void negativeAmountIsRejected() {
        assertThatThrownBy(() -> Money.inr("-1"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void amountIsNormalisedToTwoDecimalPlaces() {
        assertThat(Money.inr("1000").amount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(Money.inr("1000.005").amount()).isEqualByComparingTo(new BigDecimal("1000.01"));
    }

    @Test
    void equalAmountsInDifferentScalesAreEqual() {
        assertThat(Money.inr("1000")).isEqualTo(Money.inr("1000.00"));
    }

    @Test
    void addRequiresSameCurrency() {
        Money inr = Money.inr("100");
        Money usd = new Money(new BigDecimal("100"), Currency.getInstance("USD"));

        assertThat(inr.add(inr)).isEqualTo(Money.inr("200"));
        assertThatThrownBy(() -> inr.add(usd)).isInstanceOf(ValidationException.class);
    }

    @Test
    void multiplyRejectsNegativeFactor() {
        assertThat(Money.inr("100").multiply(3)).isEqualTo(Money.inr("300"));
        assertThatThrownBy(() -> Money.inr("100").multiply(-1))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void percentageRoundsHalfUp() {
        assertThat(Money.inr("1000").percentage(50)).isEqualTo(Money.inr("500.00"));
        assertThat(Money.inr("333").percentage(50)).isEqualTo(Money.inr("166.50"));
        assertThatThrownBy(() -> Money.inr("100").percentage(101))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void isWithinTreatsNullBoundsAsUnbounded() {
        Money amount = Money.inr("500");

        assertThat(amount.isWithin(null, null)).isTrue();
        assertThat(amount.isWithin(Money.inr("400"), null)).isTrue();
        assertThat(amount.isWithin(null, Money.inr("600"))).isTrue();
        assertThat(amount.isWithin(Money.inr("600"), null)).isFalse();
        assertThat(amount.isWithin(null, Money.inr("400"))).isFalse();
        // Bounds are inclusive.
        assertThat(amount.isWithin(Money.inr("500"), Money.inr("500"))).isTrue();
    }
}
