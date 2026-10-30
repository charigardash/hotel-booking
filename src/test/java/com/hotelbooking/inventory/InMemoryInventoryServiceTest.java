package com.hotelbooking.inventory;

import com.hotelbooking.common.DateRange;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.common.exception.ValidationException;
import com.hotelbooking.inventory.impl.InMemoryInventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryInventoryServiceTest {

    private static final LocalDate DAY_1 = LocalDate.of(2026, 3, 10);
    private static final String RT = "rt-1";

    private InMemoryInventoryService inventory;

    @BeforeEach
    void setUp() {
        inventory = new InMemoryInventoryService();
        inventory.register(RT, 2);
    }

    private static DateRange range(LocalDate in, int nights) {
        return new DateRange(in, in.plusDays(nights));
    }

    // ---- register ----

    @Test
    void registerRejectsNonPositiveCapacity() {
        assertThatThrownBy(() -> inventory.register("rt-x", 0))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void registerTwiceForSameRoomTypeIsRejected() {
        assertThatThrownBy(() -> inventory.register(RT, 5))
                .isInstanceOf(ValidationException.class);
    }

    // ---- basic reserve / release ----

    @Test
    void freshRoomTypeHasFullAvailability() {
        assertThat(inventory.availableUnits(RT, DAY_1)).isEqualTo(2);
        assertThat(inventory.isAvailable(RT, range(DAY_1, 3), 2)).isTrue();
    }

    @Test
    void reserveHoldsEveryNightInTheStay() {
        DateRange stay = range(DAY_1, 3);
        assertThat(inventory.reserve(RT, stay, 2)).isTrue();

        assertThat(inventory.availableUnits(RT, DAY_1)).isZero();
        assertThat(inventory.availableUnits(RT, DAY_1.plusDays(1))).isZero();
        assertThat(inventory.availableUnits(RT, DAY_1.plusDays(2))).isZero();
        // Checkout day itself isn't held.
        assertThat(inventory.availableUnits(RT, DAY_1.plusDays(3))).isEqualTo(2);
    }

    @Test
    void reserveFailsWhenAnySingleNightIsSoldOut() {
        inventory.reserve(RT, range(DAY_1.plusDays(1), 1), 2); // sell out night 2

        // 3-night stay from DAY_1 hits night 2, which is full.
        assertThat(inventory.reserve(RT, range(DAY_1, 3), 1)).isFalse();

        // And the failure must not have held the other nights.
        assertThat(inventory.availableUnits(RT, DAY_1)).isEqualTo(2);
        assertThat(inventory.availableUnits(RT, DAY_1.plusDays(2))).isEqualTo(2);
    }

    @Test
    void releaseFreesUnitsOnEveryNight() {
        DateRange stay = range(DAY_1, 2);
        inventory.reserve(RT, stay, 2);

        inventory.release(RT, stay, 2);
        assertThat(inventory.availableUnits(RT, DAY_1)).isEqualTo(2);
        assertThat(inventory.availableUnits(RT, DAY_1.plusDays(1))).isEqualTo(2);
    }

    @Test
    void releasingWhenNothingIsHeldIsANoOp() {
        inventory.release(RT, range(DAY_1, 2), 1); // no prior reserve
        assertThat(inventory.availableUnits(RT, DAY_1)).isEqualTo(2);
    }

    @Test
    void releasingMoreThanHeldClampsToZeroRatherThanGoingNegative() {
        inventory.reserve(RT, range(DAY_1, 1), 1);
        inventory.release(RT, range(DAY_1, 1), 5); // release more than held

        assertThat(inventory.availableUnits(RT, DAY_1)).isEqualTo(2);
    }

    // ---- half-open range check at the inventory layer ----

    @Test
    void adjacentStaysDoNotConflict() {
        assertThat(inventory.reserve(RT, range(DAY_1, 2), 2)).isTrue();
        // Next guest checks in on the day the first guest checks out.
        assertThat(inventory.reserve(RT, range(DAY_1.plusDays(2), 2), 2)).isTrue();
    }

    // ---- validation & errors ----

    @Test
    void reserveRejectsNonPositiveUnits() {
        assertThatThrownBy(() -> inventory.reserve(RT, range(DAY_1, 1), 0))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void reserveOnUnknownRoomTypeThrows() {
        assertThatThrownBy(() -> inventory.reserve("nope", range(DAY_1, 1), 1))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
