package com.hotelbooking.property.service;

import com.hotelbooking.common.Money;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.common.exception.ValidationException;
import com.hotelbooking.inventory.impl.InMemoryInventoryService;
import com.hotelbooking.property.domain.*;
import com.hotelbooking.property.repository.impl.InMemoryOwnerRepository;
import com.hotelbooking.property.repository.impl.InMemoryPropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyServiceTest {

    private PropertyService service;
    private InMemoryInventoryService inventory;

    @BeforeEach
    void setUp() {
        inventory = new InMemoryInventoryService();
        service = new PropertyService(new InMemoryPropertyRepository(),
                new InMemoryOwnerRepository(), inventory);
    }

    private CreatePropertyCommand validCommand(String ownerId) {
        return new CreatePropertyCommand(ownerId, "Sea View",
                new Address("1 Beach Rd", "Bandra", "Mumbai", "MH", "400050"),
                4, Set.of(Amenity.WIFI),
                List.of(new RoomTypeSpec("Deluxe", 3, Money.inr("2000"), 2)));
    }

    @Test
    void createOwnerAssignsAnId() {
        Owner owner = service.createOwner("Chain Co", "chain@example.com");
        assertThat(owner.id()).isNotBlank();
    }

    @Test
    void createPropertyRequiresExistingOwner() {
        assertThatThrownBy(() -> service.createProperty(validCommand("missing-owner")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createPropertyRequiresAtLeastOneRoomType() {
        String ownerId = service.createOwner("Chain Co", "chain@example.com").id();
        CreatePropertyCommand emptyRooms = new CreatePropertyCommand(ownerId, "Sea View",
                new Address("1 Beach Rd", "Bandra", "Mumbai", "MH", "400050"),
                4, Set.of(), List.of());

        assertThatThrownBy(() -> service.createProperty(emptyRooms))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createPropertyRegistersInventoryForEachRoomType() {
        String ownerId = service.createOwner("Chain Co", "chain@example.com").id();
        Property property = service.createProperty(validCommand(ownerId));

        RoomType room = property.roomTypes().iterator().next();
        // Registered with the correct capacity, so nothing has been consumed yet.
        assertThat(inventory.availableUnits(room.id(), java.time.LocalDate.now().plusDays(1)))
                .isEqualTo(2);
    }

    @Test
    void soloOwnerIsJustAnOwnerWithOneProperty() {
        String ownerId = service.createOwner("Solo", "solo@example.com").id();
        Property property = service.createProperty(validCommand(ownerId));

        assertThat(property.ownerId()).isEqualTo(ownerId);
        assertThat(service.propertiesOf(ownerId)).hasSize(1);
    }

    @Test
    void chainOwnerCanHaveManyProperties() {
        String ownerId = service.createOwner("Chain Co", "chain@example.com").id();
        service.createProperty(validCommand(ownerId));
        service.createProperty(validCommand(ownerId));
        service.createProperty(validCommand(ownerId));

        assertThat(service.propertiesOf(ownerId)).hasSize(3);
    }
}
