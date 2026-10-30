package com.hotelbooking.property.domain;

import com.hotelbooking.common.Money;
import com.hotelbooking.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyTest {

    private static Address address() {
        return new Address("1 Beach Rd", "Bandra", "Mumbai", "MH", "400050");
    }

    private static RoomType deluxe() {
        return new RoomType("rt-deluxe", "Deluxe", 3, Money.inr("2000"), 5);
    }

    private static RoomType suite() {
        return new RoomType("rt-suite", "Suite", 4, Money.inr("4000"), 2);
    }

    private static Property property() {
        return new Property("p1", "o1", "Sea View", address(), 4,
                Set.of(Amenity.WIFI, Amenity.POOL), List.of(deluxe(), suite()));
    }

    @Test
    void starRatingMustBeBetweenOneAndFive() {
        assertThatThrownBy(() -> new Property("p1", "o1", "Sea View", address(), 0,
                Set.of(), List.of(deluxe())))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new Property("p1", "o1", "Sea View", address(), 6,
                Set.of(), List.of(deluxe())))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void requiresAtLeastOneRoomType() {
        assertThatThrownBy(() -> new Property("p1", "o1", "Sea View", address(), 4,
                Set.of(), List.of()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new Property("p1", "o1", "Sea View", address(), 4,
                Set.of(), null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void rejectsDuplicateRoomTypeIds() {
        assertThatThrownBy(() -> new Property("p1", "o1", "Sea View", address(), 4,
                Set.of(), List.of(deluxe(), deluxe())))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void roomTypeLookupById() {
        Property property = property();
        assertThat(property.roomType("rt-deluxe")).contains(deluxe());
        assertThat(property.roomType("nope")).isEmpty();
    }

    @Test
    void hasAllAmenitiesRequiresEveryOne() {
        Property property = property();
        assertThat(property.hasAllAmenities(Set.of(Amenity.WIFI))).isTrue();
        assertThat(property.hasAllAmenities(Set.of(Amenity.WIFI, Amenity.POOL))).isTrue();
        assertThat(property.hasAllAmenities(Set.of(Amenity.WIFI, Amenity.GYM))).isFalse();
        // Null / empty means "no requirement".
        assertThat(property.hasAllAmenities(null)).isTrue();
        assertThat(property.hasAllAmenities(Set.of())).isTrue();
    }

    @Test
    void roomTypesCollectionReflectsConstructorInput() {
        Property property = property();
        assertThat(property.roomTypes())
                .extracting(RoomType::id)
                .containsExactlyInAnyOrder("rt-deluxe", "rt-suite");
    }
}
