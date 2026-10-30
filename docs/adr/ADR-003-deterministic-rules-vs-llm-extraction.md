# ADR-003: Deterministic Rules Engine vs. LLM Extraction Isolation

## Status
Accepted

## Context and Problem Statement
Credit card issuers and banking partners publish reward structures, milestone tiers, category multipliers, and exclusion clauses inside dense, unstructured Terms & Conditions (T&C) PDF documents (often 20 to 50 pages long). These documents are updated frequently as banks adjust reward programs, impose MCC exclusions, or revise spend caps.

Modern Large Language Models (LLMs), supported by frameworks like Spring AI and vector search (pgvector), excel at semantic understanding, table extraction, and unstructured document analysis.

However, payment routing engines operate on strict operational constraints:
*   **Latency Constraint**: Point-of-sale routing requires sub-15ms execution. LLM inference takes 500ms to 4,000ms per prompt.
*   **Determinism Constraint**: Financial reward yields must be mathematically reproducible, verifiable, and free from stochastic hallucination.
*   **Cost and Scalability**: Processing thousands of transactions per second through LLM APIs incurs unsustainable token costs.

## Decision Drivers
*   Enforce a sub-15ms deterministic P99 response time for all payment swipe evaluations.
*   Guarantee 100% mathematical auditability for financial yield calculations.
*   Automate the labor-intensive ingestion of bank T&C PDF policy documents without introducing operational latency into the payment path.

## Considered Options
1.  **Direct LLM Evaluation on Transaction Path**: Pass the transaction context and card T&C embeddings directly to an LLM at runtime to select the optimal card.
2.  **Fully Manual Policy Administration**: Require human operations analysts to manually transcribe PDF rules into database tables via administrative UIs.
3.  **Strict Architectural Decoupling (Offline AI Ingestion with Deterministic Runtime Engine)**: Use Spring AI + pgvector strictly in an offline, asynchronous pipeline (`cardroute-policy-ingestor`) to compile unstructured PDFs into validated, deterministic JSON rule definitions. The runtime engine (`cardroute-engine-core`) executes exclusively against compiled deterministic rules.

## Decision
We select **Option 3: Strict Architectural Decoupling (Offline AI Ingestion with Deterministic Runtime Engine)**.

### Architectural Rationale
*   **Offline Ingestion Module (`cardroute-policy-ingestor`)**:
    *   Ingests PDF documents, splits them into semantic chunks, and generates vector embeddings stored in PostgreSQL using `pgvector`.
    *   Prompts an LLM (via Spring AI) with structured output parsing (JSON Schema) to extract:
        1. Base reward rate per currency unit.
        2. Accelerated MCC categories and their respective multipliers.
        3. Monthly and billing-cycle spend caps.
        4. Explicit MCC and merchant keyword exclusion lists.
        5. Milestone reward thresholds.
    *   Human-in-the-loop (HITL) approval gate validates the extracted schema before it is marked `ACTIVE`.
*   **Hot-Path Runtime Engine (`cardroute-engine-core`)**:
    *   Reads the active, compiled JSON rule definition cached in memory and Redis.
    *   Executes purely deterministic, arithmetic rule evaluations (MCC matching, cap tracking, yield arithmetic).
    *   Zero network calls to LLM endpoints on the transaction path.

### Latency and Reliability Comparison
| Metric | Real-Time LLM Path | Compiled Deterministic Path |
| :--- | :--- | :--- |
| P50 Latency | 850ms | 1.2ms |
| P99 Latency | 3,200ms | 4.8ms |
| Determinism | Stochastic (non-deterministic) | 100% Deterministic |
| Token Cost / Swipe | \$0.002 to \$0.015 | \$0.000 |
| Payment SLA Compliance | Fails (< 50ms required) | Passes (< 15ms target) |

## Rejected Options
*   **Direct LLM Evaluation on Transaction Path**: Violates payment gateway timeout budgets by orders of magnitude, introduces financial liability from hallucinated reward rates, and is cost-prohibitive.
*   **Fully Manual Policy Administration**: Rejected because manual policy entry introduces multi-week operational backlogs when banks revise card reward catalogues.

## Consequences
*   **Positive**: Real-time transaction evaluation runs at sub-15ms latency with 100% mathematical accuracy. High-level AI automation is leveraged where it belongs: offline document parsing.
*   **Negative**: Policy updates are not instantaneous; they follow an offline compilation and verification workflow before publishing to the active engine registry.

