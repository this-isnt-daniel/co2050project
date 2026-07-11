package com.forensicdept.document.controller;

import com.forensicdept.document.dto.DocumentResponse;
import com.forensicdept.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.forensicdept.user.repository.UserRepository;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Documents", description = "Document upload and retrieval (file metadata only)")
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "List documents for an owner (e.g. ownerType=MLEF&ownerId=1)")
    public ResponseEntity<Page<DocumentResponse>> findByOwner(
            @RequestParam String ownerType,
            @RequestParam Long ownerId,
            Pageable pageable) {
        return ResponseEntity.ok(documentService.findByOwner(ownerType, ownerId, pageable));
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload a document file and record its metadata")
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam String ownerType,
            @RequestParam Long ownerId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow().getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.upload(ownerType, ownerId, file, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document (ADMIN/JMO only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
