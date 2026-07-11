package com.forensicdept.casemanagement.controller;

import com.forensicdept.casemanagement.dto.CaseRequest;
import com.forensicdept.casemanagement.dto.CaseResponse;
import com.forensicdept.casemanagement.service.CaseService;
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
@RequestMapping("/api/cases")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cases", description = "Case intake and management")
public class CaseController {

    private final CaseService caseService;

    @GetMapping
    @Operation(summary = "Search cases by status, type, or assigned doctor")
    public ResponseEntity<Page<CaseResponse>> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String caseType,
            @RequestParam(required = false) Long doctorId,
            Pageable pageable) {
        return ResponseEntity.ok(caseService.findAll(status, caseType, doctorId, pageable));
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get all cases assigned to a specific doctor")
    public ResponseEntity<Page<CaseResponse>> findByDoctor(
            @PathVariable Long doctorId,
            Pageable pageable) {
        return ResponseEntity.ok(caseService.findByDoctor(doctorId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get case by ID")
    public ResponseEntity<CaseResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(caseService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new case")
    public ResponseEntity<CaseResponse> create(@Valid @RequestBody CaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(caseService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update case details")
    public ResponseEntity<CaseResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody CaseRequest request) {
        return ResponseEntity.ok(caseService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update case status (ADMIN/JMO only)")
    public ResponseEntity<CaseResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(caseService.updateStatus(id, status));
    }
}
