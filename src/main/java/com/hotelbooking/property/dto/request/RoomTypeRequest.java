package com.hotelbooking.property.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RoomTypeRequest(@NotBlank String name,
                              @Min(1) int maxGuests,
                              @NotNull @DecimalMin("0.0") BigDecimal basePrice,
                              @Min(1) int totalUnits) {
}
