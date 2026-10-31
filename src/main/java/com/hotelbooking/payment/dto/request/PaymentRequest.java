package com.hotelbooking.payment.dto.request;

import com.hotelbooking.common.Money;
import com.hotelbooking.payment.PaymentMethod;

import java.util.Map;

public record PaymentRequest(String bookingId, Money amount, PaymentMethod method,
                             Map<String, String> attributes) {
    public PaymentRequest {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
