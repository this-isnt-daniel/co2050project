package com.forensicdept.courtreport.controller;

import com.forensicdept.courtreport.dto.CourtReportRequest;
import com.forensicdept.courtreport.dto.CourtReportResponse;
import com.forensicdept.courtreport.service.CourtReportService;
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

import java.util.List;

@RestController
@RequestMapping("/api/court-reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Court Reports", description = "Medico-legal court report tracking")
public class CourtReportController {

    private final CourtReportService courtReportService;

    @GetMapping
    @Operation(summary = "List court reports, optionally filtered by status")
    public ResponseEntity<Page<CourtReportResponse>> findAll(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(courtReportService.findAll(status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get court report by ID")
    public ResponseEntity<CourtReportResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(courtReportService.findById(id));
    }

    @GetMapping("/upcoming-trials")
    @Operation(summary = "Get court reports with upcoming trial dates (ADMIN/JMO only)")
    public ResponseEntity<List<CourtReportResponse>> upcomingTrials(
            @RequestParam(defaultValue = "14") int daysAhead) {
        return ResponseEntity.ok(courtReportService.findUpcomingTrials(daysAhead));
    }

    @PostMapping
    @Operation(summary = "Create a new court report entry")
    public ResponseEntity<CourtReportResponse> create(@Valid @RequestBody CourtReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courtReportService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a court report")
    public ResponseEntity<CourtReportResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody CourtReportRequest request) {
        return ResponseEntity.ok(courtReportService.update(id, request));
    }
}
