package com.hotelbooking.property.dto.request;

import com.hotelbooking.property.domain.Amenity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Set;

public record CreatePropertyRequest(@NotBlank String ownerId,
                                    @NotBlank String name,
                                    @NotBlank String addressLine,
                                    @NotBlank String locality,
                                    @NotBlank String city,
                                    @NotBlank String state,
                                    @NotBlank String pincode,
                                    @Min(1) @Max(5) int starRating,
                                    Set<Amenity> amenities,
                                    @NotEmpty List<@Valid RoomTypeRequest> roomTypes) {
}
