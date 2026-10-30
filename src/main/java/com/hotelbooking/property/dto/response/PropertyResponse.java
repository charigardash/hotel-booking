package com.hotelbooking.property.dto.response;

import com.hotelbooking.property.domain.Amenity;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.RoomType;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public record PropertyResponse(String id, String ownerId, String name,
                               String city, String locality,
                               int starRating, Set<Amenity> amenities,
                               List<RoomTypeResponse> roomTypes) {

    public record RoomTypeResponse(String id, String name, int maxGuests,
                                   BigDecimal basePrice, int totalUnits) {
        static RoomTypeResponse from(RoomType rt) {
            return new RoomTypeResponse(rt.id(), rt.name(), rt.maxGuests(),
                    rt.basePrice().amount(), rt.totalUnits());
        }
    }

    public static PropertyResponse from(Property p) {
        return from(p, p.roomTypes());
    }

    public static PropertyResponse from(Property p, Collection<RoomType> roomTypes) {
        return new PropertyResponse(p.id(), p.ownerId(), p.name(),
                p.address().city(), p.address().locality(),
                p.starRating(), p.amenities(),
                roomTypes.stream().map(RoomTypeResponse::from).toList());
    }
}
