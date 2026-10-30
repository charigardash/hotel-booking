package com.hotelbooking.property.domain;

import com.hotelbooking.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OwnerTest {

    @Test
    void requiresAllFields() {
        assertThatThrownBy(() -> new Owner(null, "Chain Co", "chain@example.com"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new Owner("o1", "  ", "chain@example.com"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new Owner("o1", "Chain Co", ""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void emailMustContainAtSign() {
        assertThatThrownBy(() -> new Owner("o1", "Chain Co", "not-an-email"))
                .isInstanceOf(ValidationException.class);
    }
}
