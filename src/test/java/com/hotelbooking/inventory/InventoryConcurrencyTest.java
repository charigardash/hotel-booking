package com.hotelbooking.inventory;


import com.hotelbooking.common.DateRange;
import com.hotelbooking.inventory.impl.InMemoryInventoryService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryConcurrencyTest {

    @Test
    void concurrentReservationsNeverOversell() throws Exception {
        InMemoryInventoryService inventory = new InMemoryInventoryService();
        final int totalUnits = 5;
        final int threads = 32;
        final int attempts = 100;
        inventory.register("rt-1", totalUnits);

        DateRange stay = new DateRange(
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            CountDownLatch startLine = new CountDownLatch(1);
            List<Future<Boolean>> futures = new ArrayList<>();

            for (int i = 0; i < attempts; i++) {
                futures.add(pool.submit(() -> {
                    startLine.await();               // all threads fire together
                    return inventory.reserve("rt-1", stay, 1);
                }));
            }
            startLine.countDown();

            long granted = 0;
            for (Future<Boolean> f : futures) {
                if (f.get(10, TimeUnit.SECONDS)) granted++;
            }

            assertThat(granted).isEqualTo(totalUnits);
            assertThat(inventory.availableUnits("rt-1", stay.checkIn())).isZero();
            assertThat(inventory.availableUnits("rt-1", stay.checkIn().plusDays(1))).isZero();
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void concurrentReserveAndReleaseRemainsConsistent() throws Exception {
        InMemoryInventoryService inventory = new InMemoryInventoryService();
        inventory.register("rt-1", 3);
        DateRange stay = new DateRange(
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));

        ExecutorService pool = Executors.newFixedThreadPool(16);
        try {
            CountDownLatch startLine = new CountDownLatch(1);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < 60; i++) {
                final boolean doReserve = i % 2 == 0;
                futures.add(pool.submit(() -> {
                    startLine.await();
                    if (doReserve) inventory.reserve("rt-1", stay, 1);
                    else inventory.release("rt-1", stay, 1);
                    return null;
                }));
            }
            startLine.countDown();
            for (Future<?> f : futures) f.get(10, TimeUnit.SECONDS);

            // Whatever the interleaving, held count must stay within [0, totalUnits].
            int available = inventory.availableUnits("rt-1", stay.checkIn());
            assertThat(available).isBetween(0, 3);
        } finally {
            pool.shutdownNow();
        }
    }
}
