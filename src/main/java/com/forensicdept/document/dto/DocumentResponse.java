package com.forensicdept.document.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DocumentResponse {
    private Long id;
    private String ownerType;
    private Long ownerId;
    private String fileName;
    private String fileType;
    private Long fileSizeBytes;
    private String storagePath;
    private Long uploadedById;
    private String uploadedByUsername;
    private LocalDateTime uploadedAt;
}
