package com.hotelbooking.common;

import com.hotelbooking.common.exception.ValidationException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

public record DateRange(LocalDate checkIn, LocalDate checkOut) {

    public DateRange {
        Objects.requireNonNull(checkIn, "checkIn is required");
        Objects.requireNonNull(checkOut, "checkOut is required");
        if (!checkIn.isBefore(checkOut)) {
            throw new ValidationException(
                    "checkIn (" + checkIn + ") must be strictly before checkOut (" + checkOut + ")");
        }
    }

    public long nights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public boolean overlaps(DateRange other) {
        return checkIn.isBefore(other.checkOut) && other.checkIn.isBefore(checkOut);
    }

    /** Every night the guest occupies a room. A 3-night stay has 3 nights, not 4. */
    public List<LocalDate> nightsList() {
        return checkIn.datesUntil(checkOut).toList();
    }
}
