package com.hotelbooking.property.domain;

import com.hotelbooking.common.exception.ValidationException;

public record Owner(String id, String name, String email) {

    public Owner {
        requireText(id, "id");
        requireText(name, "name");
        requireText(email, "email");
        if (!email.contains("@")) {
            throw new ValidationException("email looks invalid: " + email);
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " is required");
        }
    }
}
