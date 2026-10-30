package com.hotelbooking.property.search.filter;

import com.hotelbooking.property.search.PropertyFilter;
import com.hotelbooking.property.search.SearchCriteria;
import com.hotelbooking.inventory.InventoryService;
import com.hotelbooking.property.domain.Property;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityFilter implements PropertyFilter {

    private final InventoryService inventoryService;

    public AvailabilityFilter(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public boolean matches(Property property, SearchCriteria criteria) {
        boolean canHost = property.roomTypes().stream()
                .anyMatch(rt -> rt.canHost(criteria.guests()));
        if (!canHost) {
            return false;
        }
        if (criteria.stay() == null) {
            return true;
        }
        return property.roomTypes().stream()
                .filter(rt -> rt.canHost(criteria.guests()))
                .anyMatch(rt -> inventoryService.isAvailable(rt.id(), criteria.stay(), 1));
    }
}
