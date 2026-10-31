package com.hotelbooking.property.controller;

import com.hotelbooking.common.DateRange;
import com.hotelbooking.common.Money;
import com.hotelbooking.common.exception.ValidationException;
import com.hotelbooking.property.domain.Address;
import com.hotelbooking.property.domain.Amenity;
import com.hotelbooking.property.domain.Owner;
import com.hotelbooking.property.domain.RoomTypeSpec;
import com.hotelbooking.property.dto.request.CreateOwnerRequest;
import com.hotelbooking.property.dto.request.CreatePropertyRequest;
import com.hotelbooking.property.dto.response.PropertyResponse;
import com.hotelbooking.property.search.PropertySearchService;
import com.hotelbooking.property.search.SearchCriteria;
import com.hotelbooking.property.service.CreatePropertyCommand;
import com.hotelbooking.property.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1")
public class PropertyController {

    private final PropertyService propertyService;
    private final PropertySearchService searchService;

    public PropertyController(PropertyService propertyService, PropertySearchService searchService) {
        this.propertyService = propertyService;
        this.searchService = searchService;
    }

    @PostMapping("/owners")
    @ResponseStatus(HttpStatus.CREATED)
    public Owner createOwner(@Valid @RequestBody CreateOwnerRequest request) {
        return propertyService.createOwner(request.name(), request.email());
    }

    @GetMapping("/owners/{ownerId}")
    public Owner getOwner(@PathVariable String ownerId) {
        return propertyService.getOwner(ownerId);
    }

    @GetMapping("/owners/{ownerId}/properties")
    public List<PropertyResponse> propertiesOf(@PathVariable String ownerId) {
        return propertyService.propertiesOf(ownerId).stream()
                .map(PropertyResponse::from)
                .toList();
    }

    @PostMapping("/properties")
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyResponse createProperty(@Valid @RequestBody CreatePropertyRequest request) {
        List<RoomTypeSpec> specs = request.roomTypes().stream()
                .map(rt -> new RoomTypeSpec(rt.name(), rt.maxGuests(),
                        Money.inr(rt.basePrice()), rt.totalUnits()))
                .toList();

        CreatePropertyCommand command = new CreatePropertyCommand(
                request.ownerId(), request.name(),
                new Address(request.addressLine(), request.locality(), request.city(),
                        request.state(), request.pincode()),
                request.starRating(), request.amenities(), specs);

        return PropertyResponse.from(propertyService.createProperty(command));
    }

    @GetMapping("/properties/{id}")
    public PropertyResponse getProperty(@PathVariable String id) {
        return PropertyResponse.from(propertyService.getProperty(id));
    }

    @GetMapping("/properties")
    public List<PropertyResponse> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String locality,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") int guests,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Set<Amenity> amenities,
            @RequestParam(required = false) Integer minStarRating) {

        DateRange stay = buildStay(checkIn, checkOut);

        SearchCriteria criteria = new SearchCriteria(city, locality, stay, guests,
                minPrice == null ? null : Money.inr(minPrice),
                maxPrice == null ? null : Money.inr(maxPrice),
                amenities, minStarRating);

        return searchService.search(criteria).stream()
                .map(result -> PropertyResponse.from(result.property(), result.availableRoomTypes()))
                .toList();
    }

    private DateRange buildStay(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null && checkOut == null) {
            return null;
        }
        if (checkIn == null || checkOut == null) {
            throw new ValidationException("Both checkIn and checkOut are required together");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new ValidationException("checkIn cannot be in the past");
        }
        return new DateRange(checkIn, checkOut);
    }
}
