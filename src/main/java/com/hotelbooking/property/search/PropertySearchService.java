package com.hotelbooking.property.search;

import com.hotelbooking.inventory.InventoryService;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.RoomType;
import com.hotelbooking.property.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertySearchService {

    /** A property plus the subset of its room types that actually satisfy the search. */
    public record SearchResult(Property property, List<RoomType> availableRoomTypes) {
    }

    private final PropertyRepository propertyRepository;
    private final List<PropertyFilter> filters;
    private final InventoryService inventoryService;

    public PropertySearchService(PropertyRepository propertyRepository,
                                 List<PropertyFilter> filters,
                                 InventoryService inventoryService) {
        this.propertyRepository = propertyRepository;
        this.filters = filters;
        this.inventoryService = inventoryService;
    }

    public List<SearchResult> search(SearchCriteria criteria) {
        return propertyRepository.findAll().stream()
                .filter(p -> filters.stream().allMatch(f -> f.matches(p, criteria)))
                .map(p -> new SearchResult(p, bookableRoomTypes(p, criteria)))
                .filter(r -> !r.availableRoomTypes().isEmpty())
                .toList();
    }

    private List<RoomType> bookableRoomTypes(Property property, SearchCriteria criteria) {
        return property.roomTypes().stream()
                .filter(rt -> rt.canHost(criteria.guests()))
                .filter(rt -> rt.basePrice().isWithin(criteria.minPrice(), criteria.maxPrice()))
                .filter(rt -> criteria.stay() == null
                        || inventoryService.isAvailable(rt.id(), criteria.stay(), 1))
                .toList();
    }
}
