# CLAUDE.md - cardroute-engine Guidelines and Architecture Specifications

This file establishes strict operational, architectural, and development standards for `cardroute-engine`. Human developers and automated AI coding agents must adhere to the invariants defined herein.

---

## 1. Project Overview & Domain Vocabulary

`cardroute-engine` is a distributed, high-throughput, low-latency financial routing and reward optimization platform. It calculates optimal payment instruments (credit cards and UPI payment routing) at the point of transaction initiation, dynamically evaluating Merchant Category Codes (MCC), tiered milestone bonuses, category spend caps, and exclusion criteria.

### Core Domain Vocabulary

*   **Merchant Category Code (MCC)**: A standardized 4-digit numeric identifier (ISO 18245) classifying the primary commercial activity of a merchant (e.g., `5411` Grocery Stores, `5812` Restaurants, `5541` Fuel Dispensary). Determines base reward eligibility and accelerated multiplier categories.
*   **Spend Cap**: A discrete temporal boundary (typically billing-cycle or monthly) governing the maximum expenditure eligible for accelerated reward rates on a given card category. Spend beyond this cap falls back to the card base yield or zero yield.
*   **Milestone Tier**: A step-function spend target (e.g., 100,000 INR quarterly spend) that triggers an incremental lump-sum reward bonus or fee waiver upon achievement.
*   **Exclusion Rule**: A deterministic constraint disqualifying specific transactions from reward accumulation regardless of merchant category (e.g., fuel surcharge transactions, digital wallet reloads, tax payments, rent disbursements).
*   **Net Reward Yield**: The calculated net percentage or absolute financial value returned to the cardholder after deducting merchant platform surcharges, forex markup, and convenience fees from gross reward value.

---

## 2. Build, Lint, and Test Commands

The repository uses Apache Maven with Java 21 and Spring Boot 3.4.

### Build and Compilation
```bash
# Clean and compile all modules with Java 21 toolchain
./mvnw clean compile

# Full multi-module verification and packaging
./mvnw clean verify
```

### Test Execution
```bash
# Execute all unit tests across all modules
./mvnw test

# Execute a single unit or integration test class
./mvnw test -Dtest=TransactionEvaluationServiceIntegrationTest

# Execute a specific test method within a class
./mvnw test -Dtest=TransactionEvaluationServiceIntegrationTest#shouldEnforceSpendCapUnderConcurrentRequests

# Execute integration tests only (Failsafe plugin)
./mvnw verify -DskipUnitTests
```

### Code Quality and Verification
```bash
# Check formatting against Google Java Style
./mvnw spotless:check

# Apply formatting automatically
./mvnw spotless:apply
```

### Testcontainers Prerequisites
*   Docker daemon must be operational (Docker Engine, Colima, or OrbStack).
*   Testcontainers relies on Ryuk (`testcontainers/ryuk`) for resource cleanup.
*   Ensure environment variables `DOCKER_HOST` or default unix socket `/var/run/docker.sock` are accessible.

---

## 3. Strict Architectural Invariants

Every modification, addition, and pull request must strictly comply with the following architectural rules:

### 3.1. Hexagonal / Clean Architecture Boundaries
Modules and packages are organized strictly into domain, application, and adapters:
*   **Domain Core (`domain.model`, `domain.service`)**: Contains business entities, domain aggregates, and business rules. Zero dependencies on external frameworks (no Spring annotations, no JPA annotations, no database or HTTP libraries).
*   **Ports (`domain.port.in`, `domain.port.out`)**: Java interfaces declaring use-case entrypoints (inbound) and outbound contracts (persistence, cache, external messaging).
*   **Application Services**: Orchestrate domain logic and interact solely through ports.
*   **Adapters (`adapter.in.rest`, `adapter.in.grpc`, `adapter.out.persistence`, `adapter.out.redis`)**: Implement ports and interact with protocols, databases, and caches. Adapters may depend on Spring and infrastructural libraries.

### 3.2. Zero `@Autowired` Field Injection
*   Field injection using `@Autowired` is prohibited across all classes.
*   Use constructor injection exclusively.
*   Classes may declare explicit constructors or utilize `@RequiredArgsConstructor(onConstructor_ = {@Default})` / standard final fields.
*   All injected dependencies must be declared `private final`.

### 3.3. Immutable Java `record` DTOs
*   All inbound request bodies, outbound response payloads, domain events, and query parameter aggregations must be defined as Java `record` types.
*   Records guarantee immutability, thread safety, and value-based equality semantics.
*   No mutable JavaBean getters/setters in transfer objects.

### 3.4. Explicit Custom Exceptions and RFC 7807 `ProblemDetail`
*   Business and validation failures must throw explicit, strongly-typed custom exceptions extending `CardRouteException`.
*   Generic `RuntimeException` or unstructured HTTP 500 error messages are prohibited.
*   All HTTP exceptions must be mapped centrally using `@ControllerAdvice` / `@RestControllerAdvice` into RFC 7807 `ProblemDetail` responses with standardized error codes, titles, and URI identifiers.

### 3.5. Mandatory Testcontainers Integration Testing
*   No mocked in-memory databases (such as H2 or embedded SQLite) and no mocked in-memory Redis engines are permitted for persistence and concurrency verification.
*   All repository implementations, Flyway migrations, and Redis Lua scripts must be validated using live Testcontainers instances of PostgreSQL 16+ (with `pgvector`) and Redis 7+.

### 3.6. Security and Data Leakage Prevention (DLP)
*   Primary Account Numbers (PAN), CVVs, passwords, and sensitive cardholder tokens must never be logged to STDOUT or persisted in unmasked formats.
*   All public API endpoints must enforce Jakarta Bean Validation constraints (`@Valid`, `@NotNull`, `@Positive`, `@Pattern`).

