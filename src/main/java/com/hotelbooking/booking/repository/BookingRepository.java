package com.hotelbooking.booking.repository;

import com.hotelbooking.booking.domain.Booking;

import java.util.Optional;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(String id);
}
