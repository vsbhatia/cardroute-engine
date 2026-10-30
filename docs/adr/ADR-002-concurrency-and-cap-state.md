# ADR-002: Dynamic Spend-Cap State Management and Atomic Concurrency Handling

## Status
Accepted

## Context and Problem Statement
Reward programs impose strict category spend caps within a billing cycle (e.g., 5% cashback on dining up to a maximum spend of 10,000 INR per calendar month, yielding 1% baseline rewards thereafter). 

In multi-user accounts (such as primary and secondary add-on cards) or during high-frequency parallel swipe events (such as concurrent automated bill payments or flash sales), multiple transactions arrive within milliseconds.

Consider two concurrent transactions:
*   Transaction T1: 6,000 INR at Dining (MCC 5812).
*   Transaction T2: 6,000 INR at Dining (MCC 5812).
*   Current Remaining Accelerated Cap: 8,000 INR.

If both transactions evaluate the remaining cap concurrently in an uncoordinated read-modify-write cycle:
1.  T1 reads remaining cap = 8,000 INR; evaluates all 6,000 INR at 5%.
2.  T2 reads remaining cap = 8,000 INR; evaluates all 6,000 INR at 5%.
3.  Combined accelerated spend credited: 12,000 INR against an 8,000 INR cap, causing a 4,000 INR financial leak for the issuer or partner.

We must guarantee strict atomicity, eliminate race conditions, and complete the check in under 2ms.

## Decision Drivers
*   Strict atomic read-and-decrement semantics with zero double-spend or cap breach.
*   Sub-2ms execution time for cap deduction on the critical routing path.
*   Durability of state for reconciliation, regulatory audit, and disaster recovery.
*   Resilience against temporary relational database write spikes.

## Considered Options
1.  **PostgreSQL Pessimistic Locking (`SELECT ... FOR UPDATE`)**: Lock the card category ledger row during transaction evaluation.
2.  **PostgreSQL Optimistic Locking (`@Version`)**: Read cap, evaluate, update with version check, and retry on conflict.
3.  **Distributed Lock via Redlock**: Acquire Redis distributed lock, execute read-modify-write in Java application memory, release lock.
4.  **Redis Atomic Lua Scripting with Asynchronous PostgreSQL Persistence**: Execute atomic evaluation and decrement inside a Redis Lua script in a single network round-trip, emitting domain events for asynchronous persistence into PostgreSQL.

## Decision
We select **Option 4: Redis Atomic Lua Scripting with Asynchronous PostgreSQL Persistence**.

### Architectural Rationale
*   **Single-Threaded Execution Guarantee**: Redis processes Lua scripts (`EVALSHA`) atomically on its main event loop. No other command or script can interleave between reading the remaining cap and decrementing it.
*   **Atomic Split Computation**: The Lua script computes the exact split in a single network round trip:
    *   `accelerated_amount = min(transaction_amount, remaining_cap)`
    *   `overflow_amount = transaction_amount - accelerated_amount`
    *   `new_remaining_cap = remaining_cap - accelerated_amount`
    The script atomically updates the key and returns `[new_remaining_cap, accelerated_amount, overflow_amount]`.
*   **Asynchronous Durability and Reconciliation**:
    Upon successful evaluation, the service emits a `SpendCapDeductedEvent`. An asynchronous listener (or transactional outbox worker) writes the append-only ledger record to PostgreSQL (`card_spend_ledger`). If Redis state is ever lost or corrupted, the ledger in PostgreSQL can reconstruct the Redis state via an idempotent replay script.

## Rejected Options
*   **PostgreSQL Pessimistic Locking (`SELECT FOR UPDATE`)**: High contention on a single cardholder record causes thread starvation in the database connection pool (HikariCP), driving P99 tail latency from 8ms to over 85ms.
*   **PostgreSQL Optimistic Locking**: Under concurrent swiping, the losing transaction receives an `OptimisticLockException`. Retry loops amplify database load exponentially during peak flash sale events.
*   **Distributed Lock (Redlock)**: Requires 3 network round-trips (lock acquisition, validation, lock release), adding 6-12ms of network latency to every swipe evaluation and introducing edge-case lock expiration failures during JVM stop-the-world pauses.

## Consequences
*   **Positive**: Zero race conditions, zero over-allocation of accelerated reward caps, and sub-2ms cap deduction latency.
*   **Negative**: Introduces eventual consistency between the hot-path Redis cache and the PostgreSQL relational audit ledger. Managed via periodic reconciliation workers comparing Redis balances with PostgreSQL ledger aggregates.
