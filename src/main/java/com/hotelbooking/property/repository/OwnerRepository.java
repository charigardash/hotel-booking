package com.hotelbooking.property.repository;

import com.hotelbooking.property.domain.Owner;

import java.util.Optional;

public interface OwnerRepository {
    Owner save(Owner owner);
    Optional<Owner> findById(String id);
}