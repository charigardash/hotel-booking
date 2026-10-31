package com.hotelbooking.booking.domain;

import java.util.Map;
import java.util.Set;

public enum BookingStatus {

    PENDING_PAYMENT, CONFIRMED, CANCELLED, COMPLETED;

    private static final Map<BookingStatus, Set<BookingStatus>> ALLOWED = Map.of(
            PENDING_PAYMENT, Set.of(CONFIRMED, CANCELLED),
            CONFIRMED,       Set.of(CANCELLED, COMPLETED),
            CANCELLED,       Set.of(),
            COMPLETED,       Set.of()
    );

    public boolean canTransitionTo(BookingStatus next) {
        return ALLOWED.get(this).contains(next);
    }

    public boolean isTerminal() {
        return ALLOWED.get(this).isEmpty();
    }
}
