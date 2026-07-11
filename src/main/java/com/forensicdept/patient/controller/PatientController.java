package com.forensicdept.patient.controller;

import com.forensicdept.patient.dto.PatientDeidentifiedResponse;
import com.forensicdept.patient.dto.PatientRequest;
import com.forensicdept.patient.dto.PatientResponse;
import com.forensicdept.patient.service.PatientService;
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
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Patients", description = "Patient records")
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    @Operation(summary = "List all patients (de-identified for RESEARCHER role)")
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String gender,
            Pageable pageable) {
        // The service method called depends on the authenticated role —
        // RESEARCHER calls a separate service method returning de-identified DTO
        try {
            Page<PatientResponse> result = patientService.findAll(name, gender, pageable);
            return ResponseEntity.ok(result);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            // Researcher must use /api/patients/deidentified
            Page<PatientDeidentifiedResponse> result = patientService.findAllDeidentified(pageable);
            return ResponseEntity.ok(result);
        }
    }

    @GetMapping("/deidentified")
    @Operation(summary = "De-identified patient list for RESEARCHER role only")
    public ResponseEntity<Page<PatientDeidentifiedResponse>> findAllDeidentified(Pageable pageable) {
        return ResponseEntity.ok(patientService.findAllDeidentified(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient by ID (full details — not available to RESEARCHER)")
    public ResponseEntity<PatientResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Register a new patient")
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody PatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update patient details")
    public ResponseEntity<PatientResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(patientService.update(id, request));
    }
}
