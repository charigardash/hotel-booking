package com.hotelbooking.property.domain;

import com.hotelbooking.common.Money;

public record RoomTypeSpec(String name, int maxGuests, Money basePrice, int totalUnits) {
}
