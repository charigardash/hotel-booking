package com.hotelbooking.property.repository;

import com.hotelbooking.property.domain.Property;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository {
    Property save(Property property);
    Optional<Property> findById(String id);
    List<Property> findAll();
    List<Property> findByOwnerId(String ownerId);
}
