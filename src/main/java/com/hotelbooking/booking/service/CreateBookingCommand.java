package com.hotelbooking.booking.service;

import com.hotelbooking.common.DateRange;

public record CreateBookingCommand(String propertyId, String roomTypeId, String guestName,
                                   String guestEmail, DateRange stay, int guests) {
}
