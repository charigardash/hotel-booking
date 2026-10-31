package com.hotelbooking.payment.paymentprocessor;

import com.hotelbooking.payment.dto.request.PaymentRequest;
import com.hotelbooking.payment.dto.response.PaymentResult;
import com.hotelbooking.payment.paymentgateway.PaymentGateway;

/**
 * Template method pattern: all gateway-based processors do the same thing,
 * they just declare a different PaymentMethod.
 */
public abstract class AbstractGatewayProcessor implements PaymentProcessor {
    protected final PaymentGateway gateway;

    protected AbstractGatewayProcessor(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public PaymentResult process(PaymentRequest request) {
        return gateway.charge(request.bookingId(), request.amount(), supportedMethod(), request.attributes());
    }
}
