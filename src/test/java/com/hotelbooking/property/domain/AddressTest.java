package com.hotelbooking.property.domain;

import com.hotelbooking.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddressTest {

    private static Address valid() {
        return new Address("1 Beach Rd", "Bandra", "Mumbai", "MH", "400050");
    }

    @Test
    void requiresEveryField() {
        assertThatThrownBy(() -> new Address(null, "Bandra", "Mumbai", "MH", "400050"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new Address("1 Beach Rd", "  ", "Mumbai", "MH", "400050"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new Address("1 Beach Rd", "Bandra", "", "MH", "400050"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void cityMatchingIsCaseInsensitive() {
        assertThat(valid().isInCity("mumbai")).isTrue();
        assertThat(valid().isInCity("MUMBAI")).isTrue();
        assertThat(valid().isInCity("Pune")).isFalse();
        assertThat(valid().isInCity(null)).isFalse();
    }

    @Test
    void localityMatchingIsCaseInsensitive() {
        assertThat(valid().isInLocality("bandra")).isTrue();
        assertThat(valid().isInLocality("Colaba")).isFalse();
    }
}
