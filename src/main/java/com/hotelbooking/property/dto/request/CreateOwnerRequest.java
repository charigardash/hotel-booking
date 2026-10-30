package com.hotelbooking.property.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateOwnerRequest(@NotBlank String name, @NotBlank @Email String email) {
}
