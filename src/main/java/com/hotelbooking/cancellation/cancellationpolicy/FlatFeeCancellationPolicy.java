package com.hotelbooking.cancellation.cancellationpolicy;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.cancellation.Refund;
import com.hotelbooking.common.Money;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class FlatFeeCancellationPolicy implements CancellationPolicy {

    @Override
    public Refund calculateRefund(Booking booking, Instant cancelledAt) {
        if (!booking.isPaid()) {
            return new Refund(Money.zeroInr(), 0, "Unpaid");
        }
        return new Refund(Money.inr("500"), 100, "Flat ₹500 refund applied");
    }
}
