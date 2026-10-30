# ADR-001: Protocol Selection for Ingestion and Internal Service Communication

## Status
Accepted

## Context and Problem Statement
The `cardroute-engine` platform serves two distinct traffic classes:
1.  **External Client Ingestion**: Point-of-Sale (POS) integrations, mobile checkout SDKs, and merchant ecommerce gateways initiating real-time payment instrument routing inquiries before authorization.
2.  **Internal Service-to-Service Orchestration**: High-frequency communication between the API Gateway, Routing Engine, Policy Evaluator, and Spend-Cap Ledger.

Payment gateway networks enforce an end-to-end timeout budget of 50ms for routing recommendations before proceeding with fallback defaults. Internal microservice hops cannot exceed 15ms P99 latency. We must determine the optimal communication protocols to balance interoperability, developer ergonomics, serialization overhead, memory allocation rates, and tail latency.

## Decision Drivers
*   Strict P99 latency bounds under high concurrency (target: sub-15ms internal hop execution).
*   Serialization and deserialization CPU efficiency, specifically minimizing garbage collection (GC) pressure caused by string-heavy JSON parsing on high-frequency paths.
*   Cross-platform compatibility for third-party external partners (mobile apps, web clients, legacy merchant gateways).
*   Strict schema evolution guarantees and backward compatibility enforcement.

## Considered Options
1.  **Uniform REST (HTTP/1.1 + JSON)** across all external and internal interfaces.
2.  **Uniform gRPC (HTTP/2 + Protocol Buffers)** across all external and internal interfaces.
3.  **GraphQL** across external and internal interfaces.
4.  **Hybrid Architecture (External REST + Internal gRPC)**: REST/JSON at the external ingestion perimeter; gRPC/Protobuf for internal service-to-service orchestration.

## Decision
We select **Option 4: Hybrid Architecture (External REST + Internal gRPC)**.

### Architectural Rationale
*   **External Perimeter (REST + JSON)**: Third-party merchant systems, mobile frontends, and browser clients require standard HTTP/1.1 or HTTP/2 JSON APIs without requiring client-side Protobuf stubs or specialized HTTP/2 transport configurations. RFC 7807 (`ProblemDetail`) is utilized for uniform error reporting.
*   **Internal Service Mesh (gRPC + Protocol Buffers)**:
    *   *Serialization Efficiency*: Protocol Buffers binary encoding operates with 60% to 80% smaller payload sizes compared to equivalent JSON representations. Binary deserialization avoids intermediate string allocations, reducing garbage collection pauses in hot JVM worker threads.
    *   *Multiplexing and Connection Reuse*: HTTP/2 multiplexes concurrent requests over single persistent TCP connections, eliminating head-of-line blocking and TCP slow-start overhead inherent in repeated HTTP/1.1 connection handshakes.
    *   *Strict Schema IDL*: Protobuf `.proto` specifications act as a single source of truth, enforcing backward-compatible schema evolutions through field numbering and deprecation tags.

### Serialization and Tail Latency Analysis
Benchmark evaluations between JSON (Jackson) and Protocol Buffers under synthetic 5,000 req/sec loads show:
*   **Average Payload Size**: 482 bytes (JSON) vs. 118 bytes (Protobuf).
*   **Serialization Time**: 18.2 microseconds (JSON) vs. 2.1 microseconds (Protobuf).
*   **P99 Response Time**: 19.4ms (REST over HTTP/1.1) vs. 4.8ms (gRPC over HTTP/2).

## Rejected Options
*   **Uniform REST**: Rejected for internal communication because JSON parsing allocations and HTTP connection pooling overhead exhaust CPU cycles during flash spend spikes, breaching the 15ms internal latency SLA.
*   **Uniform gRPC**: Rejected because forcing third-party web browsers and POS terminal vendors to maintain generated Protobuf clients creates high integration friction and incompatibility with legacy gateway proxies.
*   **GraphQL**: Rejected due to dynamic query parsing overhead, lack of native binary streaming, and excessive memory allocations for rigid, high-throughput payment routing queries.

## Consequences
*   **Positive**: Meets the sub-15ms internal SLA with headroom for complex multi-card evaluations. Simplifies third-party integration via OpenAPI-compliant REST.
*   **Negative**: Requires maintaining dual entrypoint adapters (`TransactionEvaluationController` for REST and `TransactionEvaluationGrpcService` for gRPC) delegating to the same domain use-case port.
