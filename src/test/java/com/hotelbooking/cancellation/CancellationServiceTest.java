package com.hotelbooking.cancellation;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.domain.BookingStatus;
import com.hotelbooking.booking.repository.impl.InMemoryBookingRepository;
import com.hotelbooking.cancellation.cancellationpolicy.TieredCancellationPolicy;
import com.hotelbooking.common.DateRange;
import com.hotelbooking.common.Money;
import com.hotelbooking.common.exception.InvalidStateTransitionException;
import com.hotelbooking.inventory.impl.InMemoryInventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CancellationServiceTest {

    private InMemoryBookingRepository bookingRepo;
    private InMemoryInventoryService inventory;
    private CancellationService cancellationService;

    private final String RT_ID = "rt-1";
    private Booking paidBooking;

    @BeforeEach
    void setUp() {
        bookingRepo = new InMemoryBookingRepository();
        inventory = new InMemoryInventoryService();
        cancellationService = new CancellationService(bookingRepo, inventory, new TieredCancellationPolicy());

        inventory.register(RT_ID, 1);

        // Create a booking 10 days in the future
        DateRange stay = new DateRange(LocalDate.now().plusDays(10), LocalDate.now().plusDays(12));
        paidBooking = Booking.create("b1", "p1", RT_ID, "Asha", "asha@e.com", stay, 2, Money.inr("1000"));
        paidBooking.confirm("TXN-123");
        bookingRepo.save(paidBooking);

        // Hold the inventory as if it was booked
        inventory.reserve(RT_ID, stay, 1);
    }

    @Test
    void cancellingPaidBookingReleasesInventoryAndRefunds() {
        // Cancel right now (10 days before check-in -> 100% refund)
        CancellationService.CancellationResult result = cancellationService.cancel("b1");

        // 1. State is CANCELLED
        assertThat(result.status()).isEqualTo(BookingStatus.CANCELLED);

        // 2. Refund is 100%
        assertThat(result.refund().percentage()).isEqualTo(100);
        assertThat(result.refund().amount()).isEqualTo(Money.inr("1000"));

        // 3. Inventory is released (1 unit available again)
        assertThat(inventory.availableUnits(RT_ID, paidBooking.stay().checkIn())).isEqualTo(1);
    }

    @Test
    void cannotCancelTwice() {
        cancellationService.cancel("b1");

        // Second cancel should fail because state is already terminal.
        assertThatThrownBy(() -> cancellationService.cancel("b1"))
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}
