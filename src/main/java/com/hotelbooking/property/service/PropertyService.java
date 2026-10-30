package com.hotelbooking.property.service;

import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.common.exception.ValidationException;
import com.hotelbooking.inventory.InventoryService;
import com.hotelbooking.property.domain.Owner;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.RoomType;
import com.hotelbooking.property.repository.OwnerRepository;
import com.hotelbooking.property.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final OwnerRepository ownerRepository;
    private final InventoryService inventoryService;

    public PropertyService(PropertyRepository propertyRepository,
                           OwnerRepository ownerRepository,
                           InventoryService inventoryService) {
        this.propertyRepository = propertyRepository;
        this.ownerRepository = ownerRepository;
        this.inventoryService = inventoryService;
    }

    public Owner createOwner(String name, String email) {
        return ownerRepository.save(new Owner(UUID.randomUUID().toString(), name, email));
    }

    public Owner getOwner(String id) {
        return ownerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Owner", id));
    }

    /**
     * Onboards a property. Works identically for a solo owner and for a chain:
     * the only difference is how many properties share an {@code ownerId}.
     * There is no {@code if (isChain)} branch anywhere in this class — that is
     * the design goal, not an accident.
     */
    public Property createProperty(CreatePropertyCommand command) {
        ownerRepository.findById(command.ownerId())
                .orElseThrow(() -> ResourceNotFoundException.of("Owner", command.ownerId()));

        if (command.roomTypes() == null || command.roomTypes().isEmpty()) {
            throw new ValidationException("A property must have at least one room type");
        }

        List<RoomType> roomTypes = command.roomTypes().stream()
                .map(spec -> new RoomType(UUID.randomUUID().toString(), spec.name(),
                        spec.maxGuests(), spec.basePrice(), spec.totalUnits()))
                .toList();

        Property property = new Property(UUID.randomUUID().toString(), command.ownerId(),
                command.name(), command.address(), command.starRating(),
                command.amenities(), roomTypes);

        // Persist first, register inventory second. If persistence fails, inventory
        // was never touched; a dangling inventory registration is worse than a
        // dangling property (it would be silently bookable).
        Property saved = propertyRepository.save(property);
        roomTypes.forEach(rt -> inventoryService.register(rt.id(), rt.totalUnits()));
        return saved;
    }

    public Property getProperty(String id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Property", id));
    }

    public List<Property> propertiesOf(String ownerId) {
        // Fail fast on unknown owners so callers don't get an empty list on typos.
        getOwner(ownerId);
        return propertyRepository.findByOwnerId(ownerId);
    }
}
