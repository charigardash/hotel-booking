package com.hotelbooking.booking.dto;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.domain.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingResponse(String id, String propertyId, String roomTypeId, String guestName,
                              LocalDate checkIn, LocalDate checkOut, int guests,
                              BigDecimal totalAmount, BookingStatus status,
                              String paymentId, BigDecimal refundAmount) {

    public static BookingResponse from(Booking b) {
        return new BookingResponse(b.id(), b.propertyId(), b.roomTypeId(), b.guestName(),
                b.stay().checkIn(), b.stay().checkOut(), b.guests(), b.totalAmount().amount(),
                b.status(), b.paymentId(),
                b.refundAmount() == null ? null : b.refundAmount().amount());
    }
}
