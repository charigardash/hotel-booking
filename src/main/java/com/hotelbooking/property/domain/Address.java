package com.hotelbooking.property.domain;

import com.hotelbooking.common.exception.ValidationException;

public record Address(String line1, String locality, String city, String state, String pincode) {

    public Address {
        requireText(line1, "line1");
        requireText(locality, "locality");
        requireText(city, "city");
        requireText(state, "state");
        requireText(pincode, "pincode");
    }

    public boolean isInCity(String otherCity) {
        return city.equalsIgnoreCase(otherCity);
    }

    public boolean isInLocality(String otherLocality) {
        return locality.equalsIgnoreCase(otherLocality);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " is required");
        }
    }
}
