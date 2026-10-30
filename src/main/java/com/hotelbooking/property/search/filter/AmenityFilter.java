package com.hotelbooking.property.search.filter;

import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.search.PropertyFilter;
import com.hotelbooking.property.search.SearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class AmenityFilter implements PropertyFilter {

    @Override
    public boolean matches(Property property, SearchCriteria criteria) {
        return property.hasAllAmenities(criteria.amenities());
    }
}
