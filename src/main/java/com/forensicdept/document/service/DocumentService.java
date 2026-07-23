package com.forensicdept.document.service;

import com.forensicdept.document.dto.DocumentResponse;
import com.forensicdept.document.entity.DocumentEntity;
import com.forensicdept.document.repository.DocumentRepository;
import com.forensicdept.exception.ResourceNotFoundException;
import com.forensicdept.user.entity.UserEntity;
import com.forensicdept.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final StorageService storageService;
    private final UserRepository userRepository;

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','LAB_STAFF','CLERICAL')")
    @Transactional(readOnly = true)
    public Page<DocumentResponse> findByOwner(String ownerType, Long ownerId, Pageable pageable) {
        return documentRepository.findByOwnerTypeAndOwnerId(ownerType, ownerId, pageable)
                .map(this::toResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','LAB_STAFF','CLERICAL')")
    @Transactional
    public DocumentResponse upload(String ownerType, Long ownerId, MultipartFile file, Long uploadedByUserId) {
        try {
            UserEntity uploader = userRepository.findById(uploadedByUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", uploadedByUserId));

            String subDir = ownerType.toLowerCase() + "/" + ownerId;
            String storagePath = storageService.store(file.getInputStream(), file.getOriginalFilename(), subDir);

            DocumentEntity entity = DocumentEntity.builder()
                    .ownerType(ownerType)
                    .ownerId(ownerId)
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSizeBytes(file.getSize())
                    .storagePath(storagePath)
                    .uploadedBy(uploader)
                    .uploadedAt(LocalDateTime.now())
                    .build();
            return toResponse(documentRepository.save(entity));
        } catch (IOException e) {
            throw new RuntimeException("Failed to process file upload", e);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','JMO')")
    @Transactional
    public void delete(Long id) {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
        storageService.delete(doc.getStoragePath());
        documentRepository.delete(doc);
    }

    private DocumentResponse toResponse(DocumentEntity e) {
        return DocumentResponse.builder()
                .id(e.getId())
                .ownerType(e.getOwnerType())
                .ownerId(e.getOwnerId())
                .fileName(e.getFileName())
                .fileType(e.getFileType())
                .fileSizeBytes(e.getFileSizeBytes())
                .storagePath(e.getStoragePath())
                .uploadedById(e.getUploadedBy() != null ? e.getUploadedBy().getId() : null)
                .uploadedByUsername(e.getUploadedBy() != null ? e.getUploadedBy().getUsername() : null)
                .uploadedAt(e.getUploadedAt())
                .build();
    }
}
