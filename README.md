# Delivery Box API

A REST API for managing delivery boxes and the items loaded into them, built as a take-home assessment for Polaris Digitech Limited.

A box has cameras, a battery, and a weight capacity, and can carry small items to remote locations. This service exposes endpoints to create boxes, load them with items, and query their status, enforcing weight and battery business rules along the way.

## Tech Stack

- Java 17
- Spring Boot 4.1.1
- Spring Web (REST API)
- Spring Data JPA (persistence)
- H2 Database (in-memory)
- Jakarta Bean Validation
- JUnit 5 + Mockito (testing)
- Maven

## Project Structure

```
src/main/java/com/anthony/delivery_box_api/
├── controller/     REST endpoints (BoxController)
├── service/        Business logic (BoxService)
├── repository/     Data access (BoxRepository, ItemRepository)
├── model/          Entities (Box, Item, BoxState enum)
├── exception/      Custom exceptions and global error handling
└── DeliveryBoxApiApplication.java
```

Layered architecture: controllers handle HTTP concerns only, services own all business rules, repositories handle persistence. This keeps validation logic centralized and testable independent of the web layer.

## Design Decisions & Assumptions

The brief explicitly invited assumptions where the spec was open-ended. Here's what was decided and why:

- **Box is the aggregate root.** Item-loading logic (weight and battery checks) lives in `BoxService` rather than a separate `ItemService`, since loading an item is entirely governed by the box's own state and constraints, not by any independent logic belonging to the item itself.
- **State transitions.** A box moves to `LOADING` the moment an item is successfully loaded into it. The brief does not specify exactly when a box moves to `LOADED`, `DELIVERING`, `DELIVERED`, or `RETURNING`, since those transitions depend on external triggers (e.g. a courier confirming pickup) outside this API's stated scope, they were left as valid enum states without dedicated endpoints, since building endpoints for unspecified triggers risked guessing at requirements rather than fulfilling them.
- **Battery check precedes weight check.** When loading an item, the battery-level rule is evaluated before the weight-limit rule. This reflects a physical reality: a box with insufficient battery can't safely operate its loading mechanism at all, regardless of whether the item would otherwise fit.
- **Validation at the entity level.** Constraints from the brief (max 20 characters for `txref`, max 500g weight limit, allowed character sets for `name` and `code`) are enforced via Jakarta Bean Validation annotations directly on the entities, and activated in the controller via `@Valid`. This keeps validation rules colocated with the fields they govern.
- **In-memory H2 with `create-drop`.** The database resets to the preloaded seed data on every application restart. This was a deliberate choice for reproducibility: anyone running or evaluating this project gets an identical, predictable starting state every time, rather than persisted state from a previous session.
- **Circular reference handling.** `Item` holds a back-reference to its parent `Box` (for potential future use, e.g. looking up an item's box), but this field is excluded from JSON serialization via `@JsonIgnore` to prevent infinite recursion when serializing a box's item list.
- **Error handling.** All business rule violations throw a custom `BoxException`, caught globally by a `@RestControllerAdvice` handler and converted into a consistent `400 Bad Request` JSON response (`{"error": "..."}`), rather than leaking stack traces.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|--------------|
| POST | `/boxes` | Create a new box |
| GET | `/boxes/{id}` | Get a box by ID |
| GET | `/boxes/available` | List all boxes currently in `IDLE` state |
| GET | `/boxes/{id}/battery` | Get the battery level of a box |
| POST | `/boxes/{id}/items` | Load an item into a box |
| GET | `/boxes/{id}/items` | List items currently loaded in a box |

### Sample requests

**Create a box**
```
POST /boxes
Content-Type: application/json

{
    "txref": "BOX-004",
    "weightLimit": 250,
    "batteryCapacity": 90
}
```

**Load an item**
```
POST /boxes/1/items
Content-Type: application/json

{
    "name": "test-item",
    "weight": 200,
    "code": "ITEM001"
}
```

A request that pushes total weight over the box's `weightLimit`, or targets a box with `batteryCapacity` below 25%, returns `400 Bad Request` with a descriptive error message instead of succeeding.

## Business Rules Enforced

- A box cannot be loaded with items whose combined weight exceeds its `weightLimit` (max 500g).
- A box cannot enter `LOADING` state if its `batteryCapacity` is below 25%.

## Preloaded Data

On startup, three boxes are seeded via `data.sql`:

| txref | weightLimit | batteryCapacity | state |
|-------|-------------|------------------|-------|
| BOX-001 | 500 | 100 | IDLE |
| BOX-002 | 300 | 15 | IDLE |
| BOX-003 | 400 | 80 | DELIVERING |

BOX-002's low battery and BOX-003's non-`IDLE` state are intentional, they demonstrate the battery rule and the "available boxes" filter respectively.

## Build & Run Instructions

**Requirements:** Java 17+, Maven (or use the included wrapper).

```bash
# Clone the repository
git clone https://github.com/Nebenmor/delivery-box-api.git
cd delivery-box-api

# Run the application
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

**H2 Console** (to inspect the database directly):
Visit `http://localhost:8080/h2-console` with:
- JDBC URL: `jdbc:h2:mem:deliverydb`
- Username: `sa`
- Password: (leave blank)

## Running Tests

```bash
./mvnw test
```

Unit tests cover `BoxService`'s core business logic: successful item loading, rejection on low battery, rejection on exceeded weight limit, and not-found handling, using Mockito to isolate the service layer from the database.

## Testing the API Manually

A Postman collection is included at [`postman_collection.json`](./postman_collection.json) with pre-configured requests for every endpoint, including examples that intentionally trigger both business rule rejections.

Import it into Postman via **File → Import**, then run requests against `http://localhost:8080` while the application is running.

## Author

Anthony Nebenmor
[LinkedIn](https://www.linkedin.com/in/anthony-nebenmor) · [GitHub](https://github.com/Nebenmor) · [Portfolio](https://devanthon.vercel.app)