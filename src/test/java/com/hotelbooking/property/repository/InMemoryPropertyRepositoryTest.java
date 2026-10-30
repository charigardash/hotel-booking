package com.hotelbooking.property.repository;

import com.hotelbooking.common.Money;
import com.hotelbooking.property.domain.Address;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.RoomType;
import com.hotelbooking.property.repository.impl.InMemoryPropertyRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryPropertyRepositoryTest {

    private static Property propertyWithOwner(String id, String ownerId) {
        return new Property(id, ownerId, "Sea View",
                new Address("1 Beach Rd", "Bandra", "Mumbai", "MH", "400050"),
                4, Set.of(),
                List.of(new RoomType("rt-" + id, "Deluxe", 2, Money.inr("2000"), 1)));
    }

    @Test
    void saveThenFindById() {
        InMemoryPropertyRepository repo = new InMemoryPropertyRepository();
        Property property = propertyWithOwner("p1", "o1");
        repo.save(property);

        assertThat(repo.findById("p1")).contains(property);
        assertThat(repo.findById("missing")).isEmpty();
    }

    @Test
    void findByOwnerIdReturnsOnlyThatOwnersProperties() {
        InMemoryPropertyRepository repo = new InMemoryPropertyRepository();
        repo.save(propertyWithOwner("p1", "o1"));
        repo.save(propertyWithOwner("p2", "o1"));
        repo.save(propertyWithOwner("p3", "o2"));

        assertThat(repo.findByOwnerId("o1"))
                .extracting(Property::id)
                .containsExactlyInAnyOrder("p1", "p2");
        assertThat(repo.findByOwnerId("nobody")).isEmpty();
    }

    @Test
    void findAllReturnsEverything() {
        InMemoryPropertyRepository repo = new InMemoryPropertyRepository();
        repo.save(propertyWithOwner("p1", "o1"));
        repo.save(propertyWithOwner("p2", "o2"));

        assertThat(repo.findAll()).hasSize(2);
    }
}
