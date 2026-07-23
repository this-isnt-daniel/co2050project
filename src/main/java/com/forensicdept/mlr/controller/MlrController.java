package com.forensicdept.mlr.controller;

import com.forensicdept.mlr.dto.MlrRequest;
import com.forensicdept.mlr.dto.MlrResponse;
import com.forensicdept.mlr.service.MlrService;
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
@RequestMapping("/api/mlr")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "MLR", description = "Medico-Legal Reports prepared by JMOs")
public class MlrController {

    private final MlrService mlrService;

    @GetMapping
    @Operation(summary = "List MLR records, optionally filtered by preparedBy staff ID or reportStatus")
    public ResponseEntity<Page<MlrResponse>> findAll(
            @RequestParam(required = false) Long preparedById,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(mlrService.findAll(preparedById, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get MLR by ID")
    public ResponseEntity<MlrResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(mlrService.findById(id));
    }

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get MLR by Case ID")
    public ResponseEntity<MlrResponse> findByCaseId(@PathVariable Long caseId) {
        return ResponseEntity.ok(mlrService.findByCaseId(caseId));
    }

    @PostMapping
    @Operation(summary = "Create a new Medico-Legal Report (MLR)")
    public ResponseEntity<MlrResponse> create(@Valid @RequestBody MlrRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mlrService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update MLR details (snapshots current version to revision history before edit)")
    public ResponseEntity<MlrResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody MlrRequest request,
            @RequestParam(required = false) String revisionReason,
            @RequestParam(required = false) Long revisedById) {
        return ResponseEntity.ok(mlrService.update(id, request, revisionReason, revisedById));
    }

    @PatchMapping("/{id}/finalize")
    @Operation(summary = "Finalize MLR (one-way transition, sets dateFinalized)")
    public ResponseEntity<MlrResponse> finalizeReport(@PathVariable Long id) {
        return ResponseEntity.ok(mlrService.finalizeReport(id));
    }
}
