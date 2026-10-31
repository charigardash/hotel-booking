package com.hotelbooking.booking.dto;

import com.hotelbooking.cancellation.Refund;

public record CancellationResponse(String bookingId, String status, Refund refund) {}
