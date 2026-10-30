package com.hotelbooking.property.domain;

import com.hotelbooking.common.Money;
import com.hotelbooking.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomTypeTest {

    private static RoomType deluxe() {
        return new RoomType("rt-1", "Deluxe", 3, Money.inr("2000"), 5);
    }

    @Test
    void rejectsInvalidCapacity() {
        assertThatThrownBy(() -> new RoomType("rt-1", "Deluxe", 0, Money.inr("2000"), 1))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new RoomType("rt-1", "Deluxe", 3, Money.inr("2000"), 0))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void canHostChecksCapacityAndPositivity() {
        RoomType rt = deluxe();
        assertThat(rt.canHost(1)).isTrue();
        assertThat(rt.canHost(3)).isTrue();
        assertThat(rt.canHost(4)).isFalse();
        assertThat(rt.canHost(0)).isFalse();
        assertThat(rt.canHost(-1)).isFalse();
    }

    @Test
    void priceIsPerNight() {
        assertThat(deluxe().priceFor(3)).isEqualTo(Money.inr("6000.00"));
        assertThat(deluxe().priceFor(1)).isEqualTo(Money.inr("2000.00"));
    }

    @Test
    void priceForRejectsNonPositiveNights() {
        assertThatThrownBy(() -> deluxe().priceFor(0)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> deluxe().priceFor(-2)).isInstanceOf(ValidationException.class);
    }
}
