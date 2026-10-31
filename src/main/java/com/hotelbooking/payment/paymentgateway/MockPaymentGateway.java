package com.hotelbooking.payment.paymentgateway;

import com.hotelbooking.common.Money;
import com.hotelbooking.payment.PaymentMethod;
import com.hotelbooking.payment.dto.response.PaymentResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResult charge(String bookingId, Money amount, PaymentMethod method, Map<String, String> attributes) {
        // Simulate a failure if the user passes "simulateFailure": "true"
        if (Boolean.parseBoolean(attributes.getOrDefault("simulateFailure", "false"))) {
            return PaymentResult.failure("Simulated gateway decline");
        }
        return PaymentResult.success("TXN-" + UUID.randomUUID());
    }
}
