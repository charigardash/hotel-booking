package com.hotelbooking.property.search;

import com.hotelbooking.property.domain.Property;

public interface PropertyFilter {

    /**
     * @return {@code true} if the property should be considered further given these criteria
     */
    boolean matches(Property property, SearchCriteria criteria);
}
