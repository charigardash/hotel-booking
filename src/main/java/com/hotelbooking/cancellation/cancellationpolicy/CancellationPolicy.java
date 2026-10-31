package com.hotelbooking.cancellation.cancellationpolicy;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.cancellation.Refund;

import java.time.Instant;

public interface CancellationPolicy {
    Refund calculateRefund(Booking booking, Instant cancelledAt);
}
