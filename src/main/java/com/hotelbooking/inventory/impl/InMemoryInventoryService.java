package com.hotelbooking.inventory.impl;


import com.hotelbooking.common.DateRange;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.common.exception.ValidationException;
import com.hotelbooking.inventory.InventoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class InMemoryInventoryService implements InventoryService {

    private static final class Ledger {
        private final int totalUnits;
        private final Map<LocalDate, Integer> bookedPerNight = new HashMap<>();
        private final ReentrantLock lock = new ReentrantLock();

        private Ledger(int totalUnits) {
            this.totalUnits = totalUnits;
        }
    }

    private final Map<String, Ledger> ledgers = new ConcurrentHashMap<>();

    @Override
    public void register(String roomTypeId, int totalUnits) {
        if (roomTypeId == null || roomTypeId.isBlank()) {
            throw new ValidationException("roomTypeId is required");
        }
        if (totalUnits <= 0) {
            throw new ValidationException("totalUnits must be positive, got " + totalUnits);
        }
        // putIfAbsent catches accidental double-registration instead of silently
        // wiping any bookings already held against the old ledger.
        if (ledgers.putIfAbsent(roomTypeId, new Ledger(totalUnits)) != null) {
            throw new ValidationException("inventory already registered for room type " + roomTypeId);
        }
    }

    @Override
    public boolean isAvailable(String roomTypeId, DateRange range, int units) {
        requirePositive(units);
        Ledger ledger = ledgers.get(roomTypeId);
        if (ledger == null) return false;

        ledger.lock.lock();
        try {
            return fits(ledger, range, units);
        } finally {
            ledger.lock.unlock();
        }
    }

    @Override
    public boolean reserve(String roomTypeId, DateRange range, int units) {
        requirePositive(units);
        Ledger ledger = ledgerOrThrow(roomTypeId);

        ledger.lock.lock();
        try {
            if (!fits(ledger, range, units)) {
                return false;
            }
            // Only reached when every night fits — so this loop is all-or-nothing.
            for (LocalDate night : range.nightsList()) {
                ledger.bookedPerNight.merge(night, units, Integer::sum);
            }
            return true;
        } finally {
            ledger.lock.unlock();
        }
    }

    @Override
    public void release(String roomTypeId, DateRange range, int units) {
        requirePositive(units);
        Ledger ledger = ledgerOrThrow(roomTypeId);

        ledger.lock.lock();
        try {
            for (LocalDate night : range.nightsList()) {
                // Remove the entry when the count hits zero to keep the map compact.
                ledger.bookedPerNight.computeIfPresent(night, (key, booked) -> {
                    int remaining = booked - units;
                    return remaining <= 0 ? null : remaining;
                });
            }
        } finally {
            ledger.lock.unlock();
        }
    }

    @Override
    public int availableUnits(String roomTypeId, LocalDate date) {
        if (date == null) throw new ValidationException("date is required");
        Ledger ledger = ledgers.get(roomTypeId);
        if (ledger == null) return 0;

        ledger.lock.lock();
        try {
            return ledger.totalUnits - ledger.bookedPerNight.getOrDefault(date, 0);
        } finally {
            ledger.lock.unlock();
        }
    }


    private boolean fits(Ledger ledger, DateRange range, int units) {
        for (LocalDate night : range.nightsList()) {
            int alreadyHeld = ledger.bookedPerNight.getOrDefault(night, 0);
            if (alreadyHeld + units > ledger.totalUnits) {
                return false;
            }
        }
        return true;
    }

    private Ledger ledgerOrThrow(String roomTypeId) {
        Ledger ledger = ledgers.get(roomTypeId);
        if (ledger == null) {
            throw ResourceNotFoundException.of("RoomType inventory", roomTypeId);
        }
        return ledger;
    }

    private static void requirePositive(int units) {
        if (units <= 0) {
            throw new ValidationException("units must be positive, got " + units);
        }
    }
}
