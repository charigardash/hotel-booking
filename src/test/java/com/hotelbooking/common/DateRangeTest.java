package com.hotelbooking.common;

import com.hotelbooking.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangeTest {

    private static final LocalDate DAY_1 = LocalDate.of(2026, 1, 10);
    private static final LocalDate DAY_5 = LocalDate.of(2026, 1, 14);
    private static final LocalDate DAY_10 = LocalDate.of(2026, 1, 19);

    private static DateRange range(LocalDate in, LocalDate out) {
        return new DateRange(in, out);
    }

    @Test
    void checkInMustBeStrictlyBeforeCheckOut() {
        assertThatThrownBy(() -> range(DAY_5, DAY_5)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> range(DAY_5, DAY_1)).isInstanceOf(ValidationException.class);
    }

    @Test
    void nightsIsTheDayCount() {
        assertThat(range(DAY_1, DAY_5).nights()).isEqualTo(4);
        assertThat(range(DAY_1, DAY_1.plusDays(1)).nights()).isEqualTo(1);
    }

    @Test
    void nightsListHasOneEntryPerNightAndExcludesCheckOutDay() {
        assertThat(range(DAY_1, DAY_1.plusDays(3)).nightsList())
                .containsExactly(DAY_1, DAY_1.plusDays(1), DAY_1.plusDays(2));
    }

    @Test
    void sameRangeOverlapsItself() {
        assertThat(range(DAY_1, DAY_5).overlaps(range(DAY_1, DAY_5))).isTrue();
    }

    @Test
    void containedRangeOverlaps() {
        assertThat(range(DAY_1, DAY_10).overlaps(range(DAY_5, DAY_5.plusDays(2)))).isTrue();
        assertThat(range(DAY_5, DAY_5.plusDays(2)).overlaps(range(DAY_1, DAY_10))).isTrue();
    }

    @Test
    void partiallyOverlappingRangesOverlap() {
        assertThat(range(DAY_1, DAY_5).overlaps(range(DAY_5.minusDays(1), DAY_10))).isTrue();
    }

    @Test
    void adjacentRangesDoNotOverlap() {
        // This is THE half-open guarantee: checkout day frees the room for a new checkin.
        assertThat(range(DAY_1, DAY_5).overlaps(range(DAY_5, DAY_10))).isFalse();
        assertThat(range(DAY_5, DAY_10).overlaps(range(DAY_1, DAY_5))).isFalse();
    }

    @Test
    void disjointRangesDoNotOverlap() {
        assertThat(range(DAY_1, DAY_5).overlaps(range(DAY_10, DAY_10.plusDays(2)))).isFalse();
    }
}

