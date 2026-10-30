package com.hotelbooking.property.domain;

import com.hotelbooking.common.exception.ValidationException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class Property {

    private final String id;
    private final String ownerId;
    private final String name;
    private final Address address;
    private final int starRating;
    private final Set<Amenity> amenities;
    private final Map<String, RoomType> roomTypesById;

    public Property(String id, String ownerId, String name, Address address, int starRating,
                    Set<Amenity> amenities, List<RoomType> roomTypes) {
        this.id = requireText(id, "id");
        this.ownerId = requireText(ownerId, "ownerId");
        this.name = requireText(name, "name");
        this.address = Objects.requireNonNull(address, "address is required");

        if (starRating < 1 || starRating > 5) {
            throw new ValidationException("starRating must be between 1 and 5, got " + starRating);
        }
        this.starRating = starRating;

        this.amenities = amenities == null ? Set.of() : Set.copyOf(amenities);

        if (roomTypes == null || roomTypes.isEmpty()) {
            throw new ValidationException("a property needs at least one room type");
        }
        Map<String, RoomType> byId = new LinkedHashMap<>();
        for (RoomType rt : roomTypes) {
            if (byId.putIfAbsent(rt.id(), rt) != null) {
                throw new ValidationException("duplicate room type id: " + rt.id());
            }
        }
        this.roomTypesById = Collections.unmodifiableMap(byId);
    }

    public String id() { return id; }
    public String ownerId() { return ownerId; }
    public String name() { return name; }
    public Address address() { return address; }
    public int starRating() { return starRating; }
    public Set<Amenity> amenities() { return amenities; }
    public Collection<RoomType> roomTypes() { return roomTypesById.values(); }

    public Optional<RoomType> roomType(String roomTypeId) {
        return Optional.ofNullable(roomTypesById.get(roomTypeId));
    }

    /** True when the property offers every required amenity. Null/empty = no requirement. */
    public boolean hasAllAmenities(Set<Amenity> required) {
        return required == null || amenities.containsAll(required);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " is required");
        }
        return value;
    }
}
