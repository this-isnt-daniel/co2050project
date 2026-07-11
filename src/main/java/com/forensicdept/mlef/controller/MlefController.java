package com.forensicdept.mlef.controller;

import com.forensicdept.mlef.dto.MlefRequest;
import com.forensicdept.mlef.dto.MlefResponse;
import com.forensicdept.mlef.service.MlefService;
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
@RequestMapping("/api/mlef")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "MLEF", description = "Medico-Legal Examination Forms (clinical stream)")
public class MlefController {

    private final MlefService mlefService;

    @GetMapping
    @Operation(summary = "List MLEF records, optionally filtered by doctor or status")
    public ResponseEntity<Page<MlefResponse>> findAll(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(mlefService.findAll(doctorId, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get MLEF by ID")
    public ResponseEntity<MlefResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(mlefService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new MLEF for a clinical case")
    public ResponseEntity<MlefResponse> create(@Valid @RequestBody MlefRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mlefService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing MLEF")
    public ResponseEntity<MlefResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody MlefRequest request) {
        return ResponseEntity.ok(mlefService.update(id, request));
    }
}
