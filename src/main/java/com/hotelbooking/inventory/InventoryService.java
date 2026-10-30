package com.hotelbooking.inventory;

import com.hotelbooking.common.DateRange;

import java.time.LocalDate;

public interface InventoryService {

    void register(String roomTypeId, int totalUnits);

    boolean isAvailable(String roomTypeId, DateRange range, int units);

    boolean reserve(String roomTypeId, DateRange range, int units);

    void release(String roomTypeId, DateRange range, int units);

    int availableUnits(String roomTypeId, LocalDate date);
}
