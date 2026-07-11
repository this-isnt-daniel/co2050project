package com.forensicdept.staff.controller;

import com.forensicdept.staff.dto.StaffRequest;
import com.forensicdept.staff.dto.StaffResponse;
import com.forensicdept.staff.service.StaffService;
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
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Staff", description = "Staff management")
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    @Operation(summary = "List all active staff, optionally filtered by role")
    public ResponseEntity<Page<StaffResponse>> findAll(
            @RequestParam(required = false) String role,
            Pageable pageable) {
        return ResponseEntity.ok(staffService.findAll(role, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get staff member by ID")
    public ResponseEntity<StaffResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new staff member (ADMIN only)")
    public ResponseEntity<StaffResponse> create(@Valid @RequestBody StaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a staff member (ADMIN only)")
    public ResponseEntity<StaffResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody StaffRequest request) {
        return ResponseEntity.ok(staffService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a staff member (ADMIN only)")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        staffService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
