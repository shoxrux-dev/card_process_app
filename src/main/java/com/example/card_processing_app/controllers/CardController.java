package com.example.card_processing_app.controllers;

import com.example.card_processing_app.annotations.Idempotent;
import com.example.card_processing_app.dto.request.CreateCardRequestDto;
import com.example.card_processing_app.dto.response.CardResponseDto;
import com.example.card_processing_app.entities.User;
import com.example.card_processing_app.services.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/card")
public class CardController {
    private final CardService cardService;
    private final HttpServletRequest httpRequest;

    @PreAuthorize("hasAnyRole('USER') and hasAuthority('CREATE_CARD')")
    @PostMapping("/create")
    @Idempotent
    @Operation(parameters = {
            @Parameter(
                    name = "Idempotency-Key",
                    in = ParameterIn.HEADER,
                    description = "Unique key to prevent duplicate requests (UUID)",
                    required = true,
                    schema = @Schema(type = "string", example = "550e8400-e29b-41d4-a716-446655440000")
            )
    })
    public ResponseEntity<CardResponseDto> createCard(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid CreateCardRequestDto createCardRequestDto
    ) {
        log.info("header: {}", httpRequest.getHeader("Idempotency-Key"));
        CardResponseDto cardResponseDto = cardService.createCard(user, createCardRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(cardResponseDto);
    }

    @PreAuthorize("(hasAnyRole('USER') || hasAnyRole('ADMIN')) && hasAuthority('BLOCK_CARD')")
    @PostMapping("/{cardId}/block")
    @Operation(summary = "Block card securely", description = "Requires If-Match header with current version")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Card blocked successfully"),
            @ApiResponse(responseCode = "412", description = "Precondition Failed (Version mismatch)"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> blockCard(
            @PathVariable UUID cardId,
            @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch,
            @AuthenticationPrincipal User user
    ) {
        Long version = parseETag(ifMatch);

        cardService.blockCard(cardId, user, version);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("(hasAnyRole('USER') || hasAnyRole('ADMIN')) && hasAuthority('UNBLOCK_CARD')")
    @PostMapping("/{cardId}/unblock")
    @Operation(summary = "Unblock card securely", description = "Requires If-Match header with current version")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Card unblocked successfully"),
            @ApiResponse(responseCode = "412", description = "Precondition Failed (Version mismatch)"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> unBlockCard(
            @PathVariable UUID cardId,
            @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch,
            @AuthenticationPrincipal User user
    ) {
        Long version = parseETag(ifMatch);

        cardService.unBlockCard(cardId, user, version);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('READ_CARD')")
    public ResponseEntity<List<CardResponseDto>> getAllCards(@AuthenticationPrincipal User user) {
        List<CardResponseDto> cards = cardService.getAllUserCards(user);
        return ResponseEntity.ok(cards);
    }

    private Long parseETag(String etag) {
        try {
            String cleanVersion = etag.replace("\"", "").replace("W/", "");
            return Long.parseLong(cleanVersion);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ETag format");
        }
    }
}
