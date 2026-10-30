package com.hotelbooking.property.search;

import com.hotelbooking.common.DateRange;
import com.hotelbooking.common.Money;
import com.hotelbooking.common.exception.ValidationException;
import com.hotelbooking.property.domain.Amenity;

import java.util.Set;

public record SearchCriteria(String city, String locality, DateRange stay, int guests,
                             Money minPrice, Money maxPrice, Set<Amenity> amenities,
                             Integer minStarRating) {

    public SearchCriteria {
        if (guests <= 0) {
            throw new ValidationException("guests must be positive, got " + guests);
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ValidationException("minPrice (" + minPrice + ") exceeds maxPrice (" + maxPrice + ")");
        }
        if (amenities == null) {
            amenities = Set.of();
        }
        if (minStarRating != null && (minStarRating < 1 || minStarRating > 5)) {
            throw new ValidationException("minStarRating must be between 1 and 5");
        }
    }
}
