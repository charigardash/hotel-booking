package com.hotelbooking.payment;

import com.hotelbooking.common.exception.ValidationException;
import com.hotelbooking.payment.paymentprocessor.PaymentProcessor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentProcessorRegistry {

    private final Map<PaymentMethod, PaymentProcessor> processors = new EnumMap<>(PaymentMethod.class);

    public PaymentProcessorRegistry(List<PaymentProcessor> processorList) {
        processorList.forEach(p -> processors.put(p.supportedMethod(), p));
    }

    public PaymentProcessor get(PaymentMethod method) {
        PaymentProcessor processor = processors.get(method);
        if (processor == null) {
            throw new ValidationException("Unsupported payment method: " + method);
        }
        return processor;
    }
}
