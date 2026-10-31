package com.hotelbooking.booking.service;

import com.hotelbooking.booking.repository.impl.InMemoryBookingRepository;
import com.hotelbooking.common.DateRange;
import com.hotelbooking.common.Money;
import com.hotelbooking.common.exception.ConflictException;
import com.hotelbooking.common.exception.ValidationException;
import com.hotelbooking.inventory.impl.InMemoryInventoryService;
import com.hotelbooking.property.domain.Address;
import com.hotelbooking.property.domain.Amenity;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.RoomTypeSpec;
import com.hotelbooking.property.repository.impl.InMemoryOwnerRepository;
import com.hotelbooking.property.repository.impl.InMemoryPropertyRepository;
import com.hotelbooking.property.service.CreatePropertyCommand;
import com.hotelbooking.property.service.PropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingServiceTest {

    private BookingService bookingService;
    private String propertyId;
    private String roomTypeId;
    private LocalDate checkIn = LocalDate.now().plusDays(5);
    private LocalDate checkOut = LocalDate.now().plusDays(8);

    @BeforeEach
    void setUp() {
        InMemoryPropertyRepository propertyRepo = new InMemoryPropertyRepository();
        InMemoryInventoryService inventory = new InMemoryInventoryService();

        PropertyService propertyService = new PropertyService(propertyRepo,
                new InMemoryOwnerRepository(), inventory);
        bookingService = new BookingService(new InMemoryBookingRepository(), propertyRepo, inventory);

        String ownerId = propertyService.createOwner("Chain Co", "chain@example.com").id();
        Property property = propertyService.createProperty(new CreatePropertyCommand(
                ownerId, "Sea View", new Address("1 Beach Rd", "Bandra", "Mumbai", "MH", "400050"),
                4, Set.of(Amenity.WIFI),
                List.of(new RoomTypeSpec("Deluxe", 3, Money.inr("2000"), 1)))); // Only 1 unit!

        propertyId = property.id();
        roomTypeId = property.roomTypes().iterator().next().id();
    }

    private CreateBookingCommand commandFor(LocalDate in, LocalDate out) {
        return new CreateBookingCommand(propertyId, roomTypeId, "Asha", "asha@example.com",
                new DateRange(in, out), 2);
    }

    @Test
    void overlappingBookingIsRejected() {
        // First booking succeeds.
        bookingService.createBooking(commandFor(checkIn, checkOut));

        // Second overlapping booking fails (Conflict -> 409).
        assertThatThrownBy(() -> bookingService.createBooking(
                commandFor(checkIn.plusDays(1), checkOut.plusDays(1))))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void adjacentStayIsAllowedBecauseRangeIsHalfOpen() {
        bookingService.createBooking(commandFor(checkIn, checkOut));
        // Next guest checks in on the exact day the first checks out.
        bookingService.createBooking(commandFor(checkOut, checkOut.plusDays(2)));
    }

    @Test
    void guestsExceedingRoomCapacityIsRejected() {
        CreateBookingCommand tooManyGuests = new CreateBookingCommand(propertyId, roomTypeId,
                "Asha", "asha@example.com", new DateRange(checkIn, checkOut), 9);
        assertThatThrownBy(() -> bookingService.createBooking(tooManyGuests))
                .isInstanceOf(ValidationException.class);
    }
}
