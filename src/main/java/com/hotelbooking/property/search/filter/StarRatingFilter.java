package com.hotelbooking.property.search.filter;

import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.search.PropertyFilter;
import com.hotelbooking.property.search.SearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class StarRatingFilter implements PropertyFilter {

    @Override
    public boolean matches(Property property, SearchCriteria criteria) {
        return criteria.minStarRating() == null
                || property.starRating() >= criteria.minStarRating();
    }
}