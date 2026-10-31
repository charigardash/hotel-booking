package com.hotelbooking.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateBookingRequest(@NotBlank String propertyId,
                                   @NotBlank String roomTypeId,
                                   @NotBlank String guestName,
                                   @NotBlank @Email String guestEmail,
                                   @NotNull @FutureOrPresent LocalDate checkIn,
                                   @NotNull LocalDate checkOut,
                                   @Min(1) int guests) {
}
