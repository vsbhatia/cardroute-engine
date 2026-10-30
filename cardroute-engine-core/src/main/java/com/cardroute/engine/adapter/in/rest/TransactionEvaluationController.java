/*
 * Copyright (c) 2026 cardroute-engine contributors.
 *
 * Logical Purpose:
 * Thin REST controller exposing the transaction evaluation endpoint.
 * Responsible solely for input parsing, triggering validation, delegating to the
 * domain use-case port, and mapping the domain decision to the response DTO.
 *
 * Broad Architecture Placement:
 * Inbound REST adapter in cardroute-engine-core following Hexagonal Architecture.
 */

package com.cardroute.engine.adapter.in.rest;

import com.cardroute.engine.adapter.in.rest.dto.TransactionEvaluationRequest;
import com.cardroute.engine.adapter.in.rest.dto.TransactionEvaluationResponse;
import com.cardroute.engine.adapter.in.rest.mapper.TransactionEvaluationDtoMapper;
import com.cardroute.engine.domain.model.CardRouteDecision;
import com.cardroute.engine.domain.model.EvaluationContext;
import com.cardroute.engine.domain.port.in.EvaluateTransactionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * REST controller providing real-time credit card and UPI reward routing.
 */
@RestController
@RequestMapping(path = "/api/v1/routing", produces = MediaType.APPLICATION_JSON_VALUE)
public class TransactionEvaluationController {

    private final EvaluateTransactionUseCase evaluateUseCase;
    private final TransactionEvaluationDtoMapper mapper;

    /**
     * Constructor injection. Zero @Autowired field injection allowed.
     *
     * @param evaluateUseCase Inbound port for routing evaluation.
     * @param mapper DTO to domain mapping utility.
     */
    public TransactionEvaluationController(
        EvaluateTransactionUseCase evaluateUseCase,
        TransactionEvaluationDtoMapper mapper
    ) {
        this.evaluateUseCase = Objects.requireNonNull(evaluateUseCase, "evaluateUseCase cannot be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper cannot be null");
    }

    /**
     * Evaluates candidate payment instruments and recommends the optimal card.
     *
     * @param request Validated evaluation request payload.
     * @return HTTP 200 with the optimal recommendation and yield breakdown.
     */
    @PostMapping(path = "/evaluate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransactionEvaluationResponse> evaluateTransaction(
        @Valid @RequestBody TransactionEvaluationRequest request
    ) {
        EvaluationContext context = mapper.toContext(request);
        CardRouteDecision decision = evaluateUseCase.evaluateOptimalCard(context, request.candidateCardIds());
        TransactionEvaluationResponse response = mapper.toResponse(request.transactionId(), decision);
        return ResponseEntity.ok(response);
    }
}

