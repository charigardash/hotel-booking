package com.hotelbooking.booking.dto;

import com.hotelbooking.payment.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record PayBookingRequest(@NotNull PaymentMethod method, Map<String, String> attributes) {
}
