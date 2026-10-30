package com.hotelbooking.property.service;

import com.hotelbooking.property.domain.Address;
import com.hotelbooking.property.domain.Amenity;
import com.hotelbooking.property.domain.RoomTypeSpec;

import java.util.List;
import java.util.Set;

public record CreatePropertyCommand(String ownerId, String name, Address address, int starRating,
                                    Set<Amenity> amenities, List<RoomTypeSpec> roomTypes) {
}