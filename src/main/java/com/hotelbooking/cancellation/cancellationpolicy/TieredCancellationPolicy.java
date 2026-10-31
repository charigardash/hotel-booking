package com.hotelbooking.cancellation.cancellationpolicy;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.cancellation.Refund;
import com.hotelbooking.common.Money;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Simple tiered policy:
 * >= 48h before check-in  -> 100% refund
 * >= 24h before check-in  ->  50% refund
 * < 24h before check-in   ->   0% refund
 * Unpaid bookings get 0 refund.
 */
@Component
@Primary
public class TieredCancellationPolicy implements CancellationPolicy {

    @Override
    public Refund calculateRefund(Booking booking, Instant cancelledAt) {
        if (!booking.isPaid()) {
            return new Refund(Money.zeroInr(), 0, "Booking was not paid");
        }

        Instant checkInInstant = booking.stay().checkIn()
                .atStartOfDay(ZoneId.systemDefault()).toInstant();

        long hoursBefore = Duration.between(cancelledAt, checkInInstant).toHours();

        if (hoursBefore >= 48) {
            return new Refund(booking.totalAmount(), 100, "Cancelled >= 48h before check-in");
        }
        if (hoursBefore >= 24) {
            return new Refund(booking.totalAmount().percentage(50), 50, "Cancelled between 24h and 48h before check-in");
        }
        return new Refund(Money.zeroInr(), 0, "Cancelled < 24h before check-in");
    }
}
