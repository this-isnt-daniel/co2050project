package com.forensicdept.evidence.controller;

import com.forensicdept.evidence.dto.*;
import com.forensicdept.evidence.service.EvidenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evidence")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Evidence", description = "Evidence management and chain-of-custody")
public class EvidenceController {

    private final EvidenceService evidenceService;

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get all evidence for a case (includes custody log per item)")
    public ResponseEntity<Page<EvidenceResponse>> findByCaseId(
            @PathVariable Long caseId, Pageable pageable) {
        return ResponseEntity.ok(evidenceService.findByCaseId(caseId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get evidence item by ID (includes full custody log)")
    public ResponseEntity<EvidenceResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(evidenceService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Record a new evidence item (automatically creates initial custody log entry)")
    public ResponseEntity<EvidenceResponse> create(@Valid @RequestBody EvidenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evidenceService.create(request));
    }

    @PostMapping("/custody-transfer")
    @Operation(summary = "Record a chain-of-custody transfer for an evidence item")
    public ResponseEntity<CustodyLogResponse> recordTransfer(
            @Valid @RequestBody CustodyTransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evidenceService.recordTransfer(request));
    }
}
