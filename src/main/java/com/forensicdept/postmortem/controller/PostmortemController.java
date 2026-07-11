package com.forensicdept.postmortem.controller;

import com.forensicdept.postmortem.dto.PostmortemRequest;
import com.forensicdept.postmortem.dto.PostmortemResponse;
import com.forensicdept.postmortem.service.PostmortemService;
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
@RequestMapping("/api/postmortem")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Postmortem", description = "Postmortem records (autopsy stream)")
public class PostmortemController {

    private final PostmortemService postmortemService;

    @GetMapping
    @Operation(summary = "List postmortem records, optionally filtered by doctor or cause-of-death category")
    public ResponseEntity<Page<PostmortemResponse>> findAll(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String category,
            Pageable pageable) {
        return ResponseEntity.ok(postmortemService.findAll(doctorId, category, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get postmortem record by ID")
    public ResponseEntity<PostmortemResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(postmortemService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new postmortem record for an autopsy case")
    public ResponseEntity<PostmortemResponse> create(@Valid @RequestBody PostmortemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postmortemService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing postmortem record")
    public ResponseEntity<PostmortemResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody PostmortemRequest request) {
        return ResponseEntity.ok(postmortemService.update(id, request));
    }
}
