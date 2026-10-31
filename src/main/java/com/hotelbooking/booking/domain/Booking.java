package com.hotelbooking.booking.domain;

import com.hotelbooking.common.DateRange;
import com.hotelbooking.common.Money;
import com.hotelbooking.common.exception.InvalidStateTransitionException;

import java.time.Instant;
import java.util.Objects;

public final class Booking {

    private final String id;
    private final String propertyId;
    private final String roomTypeId;
    private final String guestName;
    private final String guestEmail;
    private final DateRange stay;
    private final int guests;
    private final Money totalAmount;
    private final Instant createdAt;

    // Mutable state
    private BookingStatus status;
    private String paymentId;
    private Instant cancelledAt;
    private Money refundAmount;

    private Booking(String id, String propertyId, String roomTypeId, String guestName,
                    String guestEmail, DateRange stay, int guests, Money totalAmount) {
        this.id = Objects.requireNonNull(id);
        this.propertyId = Objects.requireNonNull(propertyId);
        this.roomTypeId = Objects.requireNonNull(roomTypeId);
        this.stay = Objects.requireNonNull(stay);
        this.totalAmount = Objects.requireNonNull(totalAmount);
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.guests = guests;
        this.createdAt = Instant.now();
        this.status = BookingStatus.PENDING_PAYMENT;
    }

    public static Booking create(String id, String propertyId, String roomTypeId, String guestName,
                                 String guestEmail, DateRange stay, int guests, Money totalAmount) {
        return new Booking(id, propertyId, roomTypeId, guestName, guestEmail, stay, guests, totalAmount);
    }

    /** Called only after a successful payment (chunk 6). */
    public synchronized void confirm(String paymentId) {
        transitionTo(BookingStatus.CONFIRMED);
        this.paymentId = paymentId;
    }

    /** Called on cancellation (chunk 7). */
    public synchronized void cancel(Money refundAmount) {
        transitionTo(BookingStatus.CANCELLED);
        this.cancelledAt = Instant.now();
        this.refundAmount = refundAmount;
    }

    private void transitionTo(BookingStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new InvalidStateTransitionException(
                    "Booking " + id + " cannot move from " + status + " to " + next);
        }
        this.status = next;
    }

    public boolean isPaid() {
        return paymentId != null;
    }

    // Getters
    public String id() { return id; }
    public String propertyId() { return propertyId; }
    public String roomTypeId() { return roomTypeId; }
    public String guestName() { return guestName; }
    public String guestEmail() { return guestEmail; }
    public DateRange stay() { return stay; }
    public int guests() { return guests; }
    public Money totalAmount() { return totalAmount; }
    public Instant createdAt() { return createdAt; }
    public BookingStatus status() { return status; }
    public String paymentId() { return paymentId; }
    public Instant cancelledAt() { return cancelledAt; }
    public Money refundAmount() { return refundAmount; }
}
