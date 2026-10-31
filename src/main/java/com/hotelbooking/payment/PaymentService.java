package com.hotelbooking.payment;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.domain.BookingStatus;
import com.hotelbooking.booking.repository.BookingRepository;
import com.hotelbooking.common.exception.InvalidStateTransitionException;
import com.hotelbooking.common.exception.PaymentFailedException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.payment.dto.request.PaymentRequest;
import com.hotelbooking.payment.dto.response.PaymentResult;
import com.hotelbooking.payment.paymentprocessor.PaymentProcessor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentProcessorRegistry registry;

    public PaymentService(BookingRepository bookingRepository, PaymentProcessorRegistry registry) {
        this.bookingRepository = bookingRepository;
        this.registry = registry;
    }

    public Booking pay(String bookingId, PaymentMethod method, Map<String, String> attributes) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", bookingId));

        // 1. Guard: Only PENDING_PAYMENT bookings can be paid.
        if (booking.status() != BookingStatus.PENDING_PAYMENT) {
            throw new InvalidStateTransitionException(
                    "Booking " + bookingId + " is " + booking.status() + " and cannot be paid");
        }

        // 2. Process payment through the correct strategy.
        PaymentProcessor processor = registry.get(method);
        PaymentResult result = processor.process(
                new PaymentRequest(booking.id(), booking.totalAmount(), method, attributes));

        // 3. Handle outcome.
        if (!result.success()) {
            // We leave the booking in PENDING_PAYMENT so the guest can try another card/method.
            throw new PaymentFailedException(result.message());
        }

        // 4. Confirm booking.
        booking.confirm(result.transactionId());
        return bookingRepository.save(booking);
    }
}
