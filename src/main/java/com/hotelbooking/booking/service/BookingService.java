package com.hotelbooking.booking.service;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.repository.BookingRepository;
import com.hotelbooking.common.Money;
import com.hotelbooking.common.exception.ConflictException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.common.exception.ValidationException;
import com.hotelbooking.inventory.InventoryService;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.RoomType;
import com.hotelbooking.property.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final InventoryService inventoryService;

    public BookingService(BookingRepository bookingRepository,
                          PropertyRepository propertyRepository,
                          InventoryService inventoryService) {
        this.bookingRepository = bookingRepository;
        this.propertyRepository = propertyRepository;
        this.inventoryService = inventoryService;
    }

    public Booking createBooking(CreateBookingCommand command) {
        if (command.stay().checkIn().isBefore(LocalDate.now())) {
            throw new ValidationException("checkIn cannot be in the past");
        }

        Property property = propertyRepository.findById(command.propertyId())
                .orElseThrow(() -> ResourceNotFoundException.of("Property", command.propertyId()));

        RoomType roomType = property.roomType(command.roomTypeId())
                .orElseThrow(() -> ResourceNotFoundException.of("RoomType", command.roomTypeId()));

        if (!roomType.canHost(command.guests())) {
            throw new ValidationException("Room type " + roomType.name()
                    + " hosts at most " + roomType.maxGuests() + " guests");
        }

        Money total = roomType.priceFor(command.stay().nights());

        // 1. ATOMIC RESERVE: Check-and-set inside the inventory service lock.
        // This prevents two concurrent requests from both seeing "available"
        // and both proceeding.
        if (!inventoryService.reserve(roomType.id(), command.stay(), 1)) {
            throw new ConflictException("No availability for room type " + roomType.name()
                    + " between " + command.stay().checkIn() + " and " + command.stay().checkOut());
        }

        try {
            // 2. PERSIST: Create and save the booking.
            Booking booking = Booking.create(UUID.randomUUID().toString(), property.id(), roomType.id(),
                    command.guestName(), command.guestEmail(), command.stay(), command.guests(), total);
            return bookingRepository.save(booking);
        } catch (RuntimeException e) {
            // 3. COMPENSATE: If saving fails, DO NOT leak the reserved inventory.
            inventoryService.release(roomType.id(), command.stay(), 1);
            throw e;
        }
    }

    public Booking getBooking(String id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", id));
    }
}
