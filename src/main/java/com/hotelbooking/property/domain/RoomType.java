package com.hotelbooking.property.domain;

import com.hotelbooking.common.Money;
import com.hotelbooking.common.exception.ValidationException;

import java.util.Objects;

public record RoomType(String id, String name, int maxGuests, Money basePrice, int totalUnits) {

    public RoomType {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(basePrice, "basePrice is required");
        if (name == null || name.isBlank()) {
            throw new ValidationException("room type name is required");
        }
        if (maxGuests <= 0) {
            throw new ValidationException("maxGuests must be positive");
        }
        if (totalUnits <= 0) {
            throw new ValidationException("totalUnits must be positive");
        }
    }

    public boolean canHost(int guests) {
        return guests > 0 && guests <= maxGuests;
    }

    /** Price for the whole stay; {@code basePrice} is per night. */
    public Money priceFor(long nights) {
        if (nights <= 0) {
            throw new ValidationException("nights must be positive");
        }
        return basePrice.multiply(nights);
    }
}
