package com.hotelbooking.booking.controller;

import com.hotelbooking.booking.dto.BookingResponse;
import com.hotelbooking.booking.dto.CancellationResponse;
import com.hotelbooking.booking.dto.CreateBookingRequest;
import com.hotelbooking.booking.dto.PayBookingRequest;
import com.hotelbooking.booking.service.BookingService;
import com.hotelbooking.booking.service.CreateBookingCommand;
import com.hotelbooking.cancellation.CancellationService;
import com.hotelbooking.cancellation.Refund;
import com.hotelbooking.common.DateRange;
import com.hotelbooking.payment.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    private final PaymentService paymentService;

    private final CancellationService cancellationService;

    public BookingController(BookingService bookingService, PaymentService paymentService, CancellationService cancellationService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.cancellationService = cancellationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse book(@Valid @RequestBody CreateBookingRequest request) {
        CreateBookingCommand command = new CreateBookingCommand(
                request.propertyId(), request.roomTypeId(), request.guestName(), request.guestEmail(),
                new DateRange(request.checkIn(), request.checkOut()), request.guests());

        return BookingResponse.from(bookingService.createBooking(command));
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable String id) {
        return BookingResponse.from(bookingService.getBooking(id));
    }

    @PostMapping("/{id}/payments")
    public BookingResponse pay(@PathVariable String id, @Valid @RequestBody PayBookingRequest request) {
        Map<String, String> attributes = request.attributes() == null ? Map.of() : request.attributes();
        return BookingResponse.from(paymentService.pay(id, request.method(), attributes));
    }

    @PostMapping("/{id}/cancel")
    public CancellationResponse cancel(@PathVariable String id) {
        var result = cancellationService.cancel(id);
        return new CancellationResponse(result.bookingId(), result.status().name(), result.refund());
    }
}
