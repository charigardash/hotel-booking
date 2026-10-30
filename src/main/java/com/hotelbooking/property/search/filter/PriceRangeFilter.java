package com.hotelbooking.property.search.filter;

import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.search.PropertyFilter;
import com.hotelbooking.property.search.SearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class PriceRangeFilter implements PropertyFilter {

    @Override
    public boolean matches(Property property, SearchCriteria criteria) {
        if (criteria.minPrice() == null && criteria.maxPrice() == null) {
            return true;
        }
        return property.roomTypes().stream()
                .anyMatch(rt -> rt.basePrice().isWithin(criteria.minPrice(), criteria.maxPrice()));
    }
}
