package com.hotelbooking.payment;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.repository.impl.InMemoryBookingRepository;
import com.hotelbooking.common.DateRange;
import com.hotelbooking.common.Money;
import com.hotelbooking.common.exception.InvalidStateTransitionException;
import com.hotelbooking.common.exception.PaymentFailedException;
import com.hotelbooking.payment.paymentgateway.MockPaymentGateway;
import com.hotelbooking.payment.paymentgateway.PaymentGateway;
import com.hotelbooking.payment.paymentprocessor.CardPaymentProcessor;
import com.hotelbooking.payment.paymentprocessor.UpiPaymentProcessor;
import com.hotelbooking.payment.paymentprocessor.WalletPaymentProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentServiceTest {

    private InMemoryBookingRepository bookingRepo;
    private PaymentService paymentService;
    private Booking pendingBooking;

    @BeforeEach
    void setUp() {
        bookingRepo = new InMemoryBookingRepository();

        // Wire up the real processors and mock gateway
        PaymentGateway gateway = new MockPaymentGateway();
        PaymentProcessorRegistry registry = new PaymentProcessorRegistry(
                List.of(new CardPaymentProcessor(gateway), new UpiPaymentProcessor(gateway), new WalletPaymentProcessor(gateway))
        );
        paymentService = new PaymentService(bookingRepo, registry);

        pendingBooking = Booking.create("b1", "p1", "rt1", "Asha", "asha@example.com",
                new DateRange(LocalDate.now().plusDays(5), LocalDate.now().plusDays(7)),
                2, Money.inr("5000"));
        bookingRepo.save(pendingBooking);
    }

    @Test
    void successfulPaymentConfirmsBooking() {
        Booking confirmed = paymentService.pay("b1", PaymentMethod.UPI, Map.of());

        assertThat(confirmed.status()).isEqualTo(com.hotelbooking.booking.domain.BookingStatus.CONFIRMED);
        assertThat(confirmed.paymentId()).startsWith("TXN-");
    }

    @Test
    void failedPaymentLeavesBookingPending() {
        Map<String, String> attrs = Map.of("simulateFailure", "true");

        assertThatThrownBy(() -> paymentService.pay("b1", PaymentMethod.CARD, attrs))
                .isInstanceOf(PaymentFailedException.class);

        assertThat(pendingBooking.status()).isEqualTo(com.hotelbooking.booking.domain.BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void cannotPayAlreadyConfirmedBooking() {
        paymentService.pay("b1", PaymentMethod.UPI, Map.of());

        assertThatThrownBy(() -> paymentService.pay("b1", PaymentMethod.UPI, Map.of()))
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}
