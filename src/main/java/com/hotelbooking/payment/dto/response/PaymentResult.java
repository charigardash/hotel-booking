package com.hotelbooking.payment.dto.response;

public record PaymentResult(boolean success, String transactionId, String message) {
    public static PaymentResult success(String transactionId) {
        return new PaymentResult(true, transactionId, "Payment successful");
    }
    public static PaymentResult failure(String message) {
        return new PaymentResult(false, null, message);
    }
}
