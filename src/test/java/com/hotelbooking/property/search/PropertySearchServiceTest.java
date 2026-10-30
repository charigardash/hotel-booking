package com.hotelbooking.property.search;

import com.hotelbooking.common.DateRange;
import com.hotelbooking.common.Money;
import com.hotelbooking.inventory.impl.InMemoryInventoryService;
import com.hotelbooking.property.domain.*;
import com.hotelbooking.property.repository.impl.InMemoryPropertyRepository;
import com.hotelbooking.property.search.filter.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PropertySearchServiceTest {

    private static final LocalDate STAY_IN = LocalDate.now().plusDays(5);
    private static final DateRange STAY = new DateRange(STAY_IN, STAY_IN.plusDays(2));

    private InMemoryPropertyRepository repo;
    private InMemoryInventoryService inventory;
    private PropertySearchService search;

    @BeforeEach
    void setUp() {
        repo = new InMemoryPropertyRepository();
        inventory = new InMemoryInventoryService();
        search = new PropertySearchService(repo,
                List.of(new LocationFilter(), new AmenityFilter(), new StarRatingFilter(),
                        new PriceRangeFilter(), new AvailabilityFilter(inventory)),
                inventory);
    }

    private Property addProperty(String id, String city, int stars, Set<Amenity> amenities,
                                 List<RoomType> roomTypes) {
        Property p = new Property(id, "o1", "Hotel " + id,
                new Address("1 St", "Locality", city, "MH", "400001"),
                stars, amenities, roomTypes);
        repo.save(p);
        roomTypes.forEach(rt -> inventory.register(rt.id(), rt.totalUnits()));
        return p;
    }

    private RoomType room(String id, int maxGuests, String price, int units) {
        return new RoomType(id, "Room-" + id, maxGuests, Money.inr(price), units);
    }

    @Test
    void noCriteriaReturnsAllProperties() {
        addProperty("p1", "Mumbai", 4, Set.of(Amenity.WIFI),
                List.of(room("r1", 2, "2000", 2)));
        addProperty("p2", "Pune", 3, Set.of(Amenity.POOL),
                List.of(room("r2", 2, "3000", 2)));

        List<PropertySearchService.SearchResult> results =
                search.search(criteriaBuilder().build());

        assertThat(results).extracting(r -> r.property().id())
                .containsExactlyInAnyOrder("p1", "p2");
    }

    @Test
    void cityFilterExcludesOtherCities() {
        addProperty("p1", "Mumbai", 4, Set.of(), List.of(room("r1", 2, "2000", 2)));
        addProperty("p2", "Pune", 4, Set.of(), List.of(room("r2", 2, "2000", 2)));

        List<PropertySearchService.SearchResult> results =
                search.search(criteriaBuilder().city("mumbai").build());

        assertThat(results).extracting(r -> r.property().id()).containsExactly("p1");
    }

    @Test
    void amenityFilterRequiresEveryAmenity() {
        addProperty("p1", "Mumbai", 4, Set.of(Amenity.WIFI, Amenity.POOL),
                List.of(room("r1", 2, "2000", 2)));
        addProperty("p2", "Mumbai", 4, Set.of(Amenity.WIFI),
                List.of(room("r2", 2, "2000", 2)));

        List<PropertySearchService.SearchResult> results =
                search.search(criteriaBuilder()
                        .amenities(Set.of(Amenity.WIFI, Amenity.POOL))
                        .build());

        assertThat(results).extracting(r -> r.property().id()).containsExactly("p1");
    }

    @Test
    void priceRangeFilterUsesRoomTypeBasePrice() {
        addProperty("p1", "Mumbai", 4, Set.of(), List.of(room("r1", 2, "1000", 2)));
        addProperty("p2", "Mumbai", 4, Set.of(), List.of(room("r2", 2, "5000", 2)));

        List<PropertySearchService.SearchResult> results =
                search.search(criteriaBuilder()
                        .minPrice(Money.inr("500"))
                        .maxPrice(Money.inr("2000"))
                        .build());

        assertThat(results).extracting(r -> r.property().id()).containsExactly("p1");
    }

    @Test
    void soldOutPropertyIsExcludedWhenDatesGiven() {
        Property p = addProperty("p1", "Mumbai", 4, Set.of(),
                List.of(room("r1", 2, "2000", 1)));
        inventory.reserve("r1", STAY, 1); // fully sold out

        List<PropertySearchService.SearchResult> results =
                search.search(criteriaBuilder().stay(STAY).build());

        assertThat(results).isEmpty();
    }

    @Test
    void capacityFilterExcludesPropertiesThatCannotHostParty() {
        addProperty("p1", "Mumbai", 4, Set.of(), List.of(room("r1", 2, "2000", 2)));
        addProperty("p2", "Mumbai", 4, Set.of(), List.of(room("r2", 4, "2000", 2)));

        List<PropertySearchService.SearchResult> results =
                search.search(criteriaBuilder().guests(4).build());

        assertThat(results).extracting(r -> r.property().id()).containsExactly("p2");
    }

    @Test
    void propertyIsExcludedWhenNoRoomTypeSatisfiesBothPriceAndAvailability() {
        // Room A: cheap but sold out. Room B: available but expensive.
        Property p = addProperty("p1", "Mumbai", 4, Set.of(),
                List.of(room("rA", 2, "1000", 1), room("rB", 2, "5000", 2)));
        inventory.reserve("rA", STAY, 1); // A sold out, B still available

        List<PropertySearchService.SearchResult> results =
                search.search(criteriaBuilder()
                        .stay(STAY)
                        .minPrice(Money.inr("500"))
                        .maxPrice(Money.inr("1500"))
                        .build());

        // Property passes both aggregate filters independently,
        // but has zero bookable room types -> must be excluded.
        assertThat(results).isEmpty();
    }

    // ---- tiny builder to keep test bodies readable ----

    private static CriteriaBuilder criteriaBuilder() {
        return new CriteriaBuilder();
    }

    private static final class CriteriaBuilder {
        private String city;
        private String locality;
        private DateRange stay;
        private int guests = 1;
        private Money minPrice;
        private Money maxPrice;
        private Set<Amenity> amenities = Set.of();
        private Integer minStarRating;

        CriteriaBuilder city(String v) { this.city = v; return this; }
        CriteriaBuilder locality(String v) { this.locality = v; return this; }
        CriteriaBuilder stay(DateRange v) { this.stay = v; return this; }
        CriteriaBuilder guests(int v) { this.guests = v; return this; }
        CriteriaBuilder minPrice(Money v) { this.minPrice = v; return this; }
        CriteriaBuilder maxPrice(Money v) { this.maxPrice = v; return this; }
        CriteriaBuilder amenities(Set<Amenity> v) { this.amenities = v; return this; }
        CriteriaBuilder minStarRating(Integer v) { this.minStarRating = v; return this; }

        SearchCriteria build() {
            return new SearchCriteria(city, locality, stay, guests,
                    minPrice, maxPrice, amenities, minStarRating);
        }
    }
}
