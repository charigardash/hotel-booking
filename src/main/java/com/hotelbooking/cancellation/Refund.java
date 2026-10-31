package com.hotelbooking.cancellation;

import com.hotelbooking.common.Money;

public record Refund(Money amount, int percentage, String reason) {
}
