package com.forensicdept.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Tracks the last assigned sequence number per document type and calendar year.
 * Used by {@link com.forensicdept.common.service.SerialNumberService} to generate
 * official serial numbers such as MLEF/2026/000001.
 *
 * <p>Rows are never deleted and numbers are never reused once assigned.</p>
 */
@Entity
@Table(name = "document_serial_sequences")
@IdClass(DocumentSerialSequenceId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DocumentSerialSequenceEntity {

    @Id
    @Column(name = "doc_type", nullable = false, length = 10)
    private String docType;

    @Id
    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "last_seq", nullable = false)
    private Integer lastSeq = 0;
}
