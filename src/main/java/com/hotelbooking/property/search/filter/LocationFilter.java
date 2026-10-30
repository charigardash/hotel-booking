package com.hotelbooking.property.search.filter;

import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.search.PropertyFilter;
import com.hotelbooking.property.search.SearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class LocationFilter implements PropertyFilter {

    @Override
    public boolean matches(Property property, SearchCriteria criteria) {
        if (criteria.city() != null && !property.address().isInCity(criteria.city())) {
            return false;
        }
        return criteria.locality() == null || property.address().isInLocality(criteria.locality());
    }
}
