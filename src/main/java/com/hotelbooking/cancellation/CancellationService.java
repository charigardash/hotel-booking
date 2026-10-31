package com.hotelbooking.cancellation;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.domain.BookingStatus;
import com.hotelbooking.booking.repository.BookingRepository;
import com.hotelbooking.cancellation.cancellationpolicy.CancellationPolicy;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.inventory.InventoryService;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CancellationService {

    public record CancellationResult(String bookingId, BookingStatus status, Refund refund) {}

    private final BookingRepository bookingRepository;
    private final InventoryService inventoryService;
    private final CancellationPolicy policy;

    public CancellationService(BookingRepository bookingRepository,
                               InventoryService inventoryService,
                               CancellationPolicy policy) {
        this.bookingRepository = bookingRepository;
        this.inventoryService = inventoryService;
        this.policy = policy;
    }

    public CancellationResult cancel(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", bookingId));

        Refund refund = policy.calculateRefund(booking, Instant.now());

        booking.cancel(refund.amount());
        bookingRepository.save(booking);

        // 3. Release the inventory hold! (Crucial)
        inventoryService.release(booking.roomTypeId(), booking.stay(), 1);

        return new CancellationResult(booking.id(), booking.status(), refund);
    }
}
