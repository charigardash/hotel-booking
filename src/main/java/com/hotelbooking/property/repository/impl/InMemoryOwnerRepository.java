package com.hotelbooking.property.repository.impl;

import com.hotelbooking.property.domain.Owner;
import com.hotelbooking.property.repository.OwnerRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOwnerRepository implements OwnerRepository {

    private final Map<String, Owner> store = new ConcurrentHashMap<>();

    @Override
    public Owner save(Owner owner) {
        store.put(owner.id(), owner);
        return owner;
    }

    @Override
    public Optional<Owner> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }
}
