package com.forensicdept.labtest.controller;

import com.forensicdept.labtest.dto.LaboratoryTestRequest;
import com.forensicdept.labtest.dto.LaboratoryTestResponse;
import com.forensicdept.labtest.service.LaboratoryTestService;
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

import java.time.LocalDate;

@RestController
@RequestMapping("/api/lab-tests")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Laboratory Tests", description = "Lab test requests and results")
public class LaboratoryTestController {

    private final LaboratoryTestService labTestService;

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get all lab tests for a case")
    public ResponseEntity<Page<LaboratoryTestResponse>> findByCaseId(
            @PathVariable Long caseId, Pageable pageable) {
        return ResponseEntity.ok(labTestService.findByCaseId(caseId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lab test by ID")
    public ResponseEntity<LaboratoryTestResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(labTestService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Request a new lab test")
    public ResponseEntity<LaboratoryTestResponse> create(@Valid @RequestBody LaboratoryTestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labTestService.create(request));
    }

    @PatchMapping("/{id}/result")
    @Operation(summary = "Update lab test result (LAB_STAFF / ADMIN only)")
    public ResponseEntity<LaboratoryTestResponse> updateResult(
            @PathVariable Long id,
            @RequestParam String result,
            @RequestParam(required = false) LocalDate resultDate) {
        return ResponseEntity.ok(labTestService.updateResult(id, result, resultDate));
    }
}
