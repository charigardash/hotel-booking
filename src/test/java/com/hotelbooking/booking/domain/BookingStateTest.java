package com.hotelbooking.booking.domain;

import com.hotelbooking.common.DateRange;
import com.hotelbooking.common.Money;
import com.hotelbooking.common.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingStateTest {

    private Booking newBooking() {
        return Booking.create("b1", "p1", "rt1", "Asha", "asha@example.com",
                new DateRange(LocalDate.now().plusDays(5), LocalDate.now().plusDays(7)),
                2, Money.inr("5000"));
    }

    @Test
    void newBookingStartsPendingPayment() {
        assertThat(newBooking().status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void confirmingMovesToConfirmed() {
        Booking booking = newBooking();
        booking.confirm("TXN-1");
        assertThat(booking.status()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.paymentId()).isEqualTo("TXN-1");
    }

    @Test
    void bookingCannotBeConfirmedTwice() {
        Booking booking = newBooking();
        booking.confirm("TXN-1");
        assertThatThrownBy(() -> booking.confirm("TXN-2"))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void cancelledBookingIsTerminal() {
        Booking booking = newBooking();
        booking.cancel(Money.zeroInr());
        assertThatThrownBy(() -> booking.confirm("TXN-1"))
                .isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(() -> booking.cancel(Money.zeroInr()))
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}
