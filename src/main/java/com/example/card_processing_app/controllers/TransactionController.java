package com.example.card_processing_app.controllers;

import com.example.card_processing_app.annotations.Idempotent;
import com.example.card_processing_app.dto.request.P2PRequest;
import com.example.card_processing_app.dto.response.TransactionResponseDto;
import com.example.card_processing_app.entities.User;
import com.example.card_processing_app.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transaction")
public class TransactionController {
    private final TransactionService transactionService;

    @PreAuthorize("hasAnyRole('USER') and hasAuthority('EXECUTE_P2P_TRANSFER')")
    @PostMapping("/p2p")
    @Idempotent
    public ResponseEntity<Void> executeP2P(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid P2PRequest request) {

        transactionService.executeP2P(request, idempotencyKey);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get card transaction history",
            description = "Retrieves a paginated list of transactions for a specific card. " +
                    "Supports pagination and sorting. Default sort: createdAt, DESC"
    )
    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping("/history/{cardId}")
    public ResponseEntity<Page<TransactionResponseDto>> getHistory(
            @Parameter(required = true)
            @PathVariable UUID cardId,
            @ParameterObject
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,

            @Parameter(hidden = true)
            @AuthenticationPrincipal User user
    ) {
        Page<TransactionResponseDto> history = transactionService.getTransactionHistoryByCardId(cardId, pageable, user);
        return ResponseEntity.ok(history);
    }

}
