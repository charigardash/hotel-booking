package com.hotelbooking.payment.paymentprocessor;

import com.hotelbooking.payment.PaymentMethod;
import com.hotelbooking.payment.paymentgateway.PaymentGateway;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentProcessor extends AbstractGatewayProcessor {
    public CardPaymentProcessor(PaymentGateway gateway) { super(gateway); }
    @Override public PaymentMethod supportedMethod() { return PaymentMethod.CARD; }
}
