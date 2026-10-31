# Hotel Booking Platform - Backend

A Spring Boot 3 / Java 17 backend service for property discovery, booking, payment, and cancellation.

This project was built with a focus on **clean architecture, domain-driven design, and extensibility**. Rather than building an exhaustive feature list, the goal was to implement a robust, thread-safe core with clear seams for future extensions.

## 🚀 How to Run

1. Ensure you have **Java 17+** and **Maven** installed.
2. Run the application from the root directory:
   ```bash
   mvn spring-boot:run
   ```
3. The service starts on `http://localhost:8080`.
4. Run the test suite (including concurrency tests):
   ```bash
   mvn test
   ```
*(Note: Persistence is in-memory and resets on application restart, as allowed by the brief).*

---

## 🛠️ API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/owners` | Create an owner account |
| `POST` | `/api/v1/properties` | Onboard a property |
| `GET` | `/api/v1/properties` | Search properties (city, dates, guests, price, amenities, star rating) |
| `POST` | `/api/v1/bookings` | Create a booking (holds inventory) |
| `POST` | `/api/v1/bookings/{id}/payments` | Pay for a booking (supports CARD, UPI, WALLET) |
| `POST` | `/api/v1/bookings/{id}/cancel` | Cancel a booking and release inventory |

### Quick End-to-End Test Script
You can copy and paste this entire block into your terminal to test the full lifecycle (onboard → search → book → pay → cancel):

```bash
# 1. Setup Owner
O_ID=$(curl -s -X POST localhost:8080/api/v1/owners -H 'Content-Type: application/json' -d '{"name":"Chain","email":"c@e.com"}' | jq -r .id)

# 2. Onboard Property (1 Room, Max 3 Guests)
P_JSON=$(curl -s -X POST localhost:8080/api/v1/properties -H 'Content-Type: application/json' -d "{\"ownerId\":\"$O_ID\",\"name\":\"Sea View\",\"addressLine\":\"1 Beach Rd\",\"locality\":\"Bandra\",\"city\":\"Mumbai\",\"state\":\"MH\",\"pincode\":\"400050\",\"starRating\":4,\"amenities\":[\"WIFI\"],\"roomTypes\":[{\"name\":\"Deluxe\",\"maxGuests\":3,\"basePrice\":2000,\"totalUnits\":1}]}")
P_ID=$(echo "$P_JSON" | jq -r .id)
RT_ID=$(echo "$P_JSON" | jq -r '.roomTypes[0].id')

# 3. Search (Should return 1 property)
echo "Search before booking:"
curl -s "localhost:8080/api/v1/properties?city=Mumbai&checkIn=2026-12-10&checkOut=2026-12-12&guests=2" | jq 'length'

# 4. Create Booking
B_ID=$(curl -s -X POST localhost:8080/api/v1/bookings -H 'Content-Type: application/json' -d "{\"propertyId\":\"$P_ID\",\"roomTypeId\":\"$RT_ID\",\"guestName\":\"Asha\",\"guestEmail\":\"asha@e.com\",\"checkIn\":\"2026-12-10\",\"checkOut\":\"2026-12-12\",\"guests\":2}" | jq -r .id)

# 5. Search (Should return 0 properties, as room is booked)
echo "Search after booking:"
curl -s "localhost:8080/api/v1/properties?city=Mumbai&checkIn=2026-12-10&checkOut=2026-12-12&guests=2" | jq 'length'

# 6. Pay for Booking (Try "simulateFailure": "true" to see a 402 error)
echo "Paying for booking:"
curl -s -X POST localhost:8080/api/v1/bookings/$B_ID/payments -H 'Content-Type: application/json' -d '{"method":"UPI"}' | jq '.status'

# 7. Cancel Booking (100% refund)
echo "Cancelling booking:"
curl -s -X POST localhost:8080/api/v1/bookings/$B_ID/cancel | jq '.status'

# 8. Search (Should return 1 property again, inventory released)
echo "Search after cancellation:"
curl -s "localhost:8080/api/v1/properties?city=Mumbai&checkIn=2026-12-10&checkOut=2026-12-12&guests=2" | jq 'length'
```

---

## 🧠 Key Design Decisions

- **Multi-Property Model:** Modeled via `Owner` 1—* `Property`. A solo owner is treated naturally as an owner with exactly one property. There is no separate code path for solo vs. chain.
- **Search Extensibility:** Search uses a `List<PropertyFilter>` injected by Spring. Adding a new filter (e.g., "Pet Friendly") requires adding one `@Component` — zero changes to `PropertySearchService`.
- **Concurrency & Double-Booking:** Handled in `InMemoryInventoryService`. It uses a `ReentrantLock` per `RoomType` to ensure that checking availability and reserving units is a single atomic operation across all nights in a stay. This is verified by multi-threaded tests.
- **Booking State Machine:** `BookingStatus` is a self-contained enum with its own transition graph. Illegal transitions (e.g., paying an already cancelled booking) are impossible by design, throwing an `InvalidStateTransitionException`.
- **Pluggable Payments:** Uses the Strategy Pattern. `PaymentProcessor` implementations (Card, UPI, Wallet) are resolved via a `PaymentProcessorRegistry`. The third-party gateway is mocked behind a `PaymentGateway` interface.
- **Pluggable Refunds:** The `CancellationPolicy` interface allows dynamic policy swapping. The default implementation is a tiered policy (100% / 50% / 0% based on time to check-in).

## 📌 Key Assumptions

- **Half-Open Date Ranges:** A stay is modeled as `[checkIn, checkOut)`. A guest checking out on the 10th frees the room for a guest checking in on the 10th.
- **Inventory Holds:** A booking immediately reserves inventory in `PENDING_PAYMENT` state. If payment fails, the inventory remains held to allow the guest to retry.
- **Money:** All amounts are represented by an immutable `Money` value object to prevent floating-point errors.

## 🔮 What I would do with more time

1. **Expiry Job:** Add a `@Scheduled` task to automatically cancel `PENDING_PAYMENT` bookings older than 15 minutes, releasing their inventory back to the pool.
2. **Idempotency:** Implement idempotency keys for the payment endpoint to prevent double-charging on network retries.
3. **Dynamic Pricing:** Introduce a `PricingStrategy` interface to allow weekend surcharges or seasonal pricing.
4. **Database Persistence:** Swap the in-memory repositories for JPA/PostgreSQL. Move the inventory locks to Redis (using Lua scripts) to handle multi-instance deployments.