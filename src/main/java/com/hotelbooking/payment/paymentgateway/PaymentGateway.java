package com.hotelbooking.payment.paymentgateway;

import com.hotelbooking.common.Money;
import com.hotelbooking.payment.PaymentMethod;
import com.hotelbooking.payment.dto.response.PaymentResult;

import java.util.Map;

/**
 * The third-party boundary. Real PSPs (Stripe, Razorpay) would be implemented here.
 * We mock it so no real money moves in tests.
 */
public interface PaymentGateway {
    PaymentResult charge(String bookingId, Money amount, PaymentMethod method, Map<String, String> attributes);
}
