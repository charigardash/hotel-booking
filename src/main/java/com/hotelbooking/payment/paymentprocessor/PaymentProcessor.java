package com.hotelbooking.payment.paymentprocessor;

import com.hotelbooking.payment.PaymentMethod;
import com.hotelbooking.payment.dto.request.PaymentRequest;
import com.hotelbooking.payment.dto.response.PaymentResult;

/**
 * One implementation per payment method.
 * Adding a method (e.g., NETBANKING) means creating a new bean that implements this.
 */
public interface PaymentProcessor {
    PaymentMethod supportedMethod();
    PaymentResult process(PaymentRequest request);
}
