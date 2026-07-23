package com.forensicdept.common.service;

import com.forensicdept.common.entity.DocumentSerialSequenceEntity;
import com.forensicdept.common.entity.DocumentSerialSequenceId;
import com.forensicdept.common.repository.SerialSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Generates official serial numbers for medico-legal documents.
 *
 * <p>Format: {@code <DOC_TYPE>/<YEAR>/<6-digit-sequence>}
 * e.g. {@code MLEF/2026/000001}, {@code EV/2026/000003}</p>
 *
 * <p>Rules:</p>
 * <ul>
 *   <li>Each document type has its own independent sequence.</li>
 *   <li>Sequences reset every calendar year.</li>
 *   <li>Numbers are never reused, even after deletions.</li>
 *   <li>Uses {@link Propagation#REQUIRES_NEW} so the counter increment is
 *       committed independently, preventing gaps on outer-transaction rollback.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class SerialNumberService {

    private final SerialSequenceRepository sequenceRepository;

    /**
     * Atomically increments the sequence counter for the given document type
     * in the current calendar year and returns the formatted serial number.
     *
     * @param docType e.g. "MLEF", "MLR", "EV", "CRT", "CASE"
     * @return formatted serial, e.g. "MLEF/2026/000001"
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String nextSerial(String docType) {
        int year = LocalDate.now().getYear();
        DocumentSerialSequenceId pk = new DocumentSerialSequenceId(docType, year);

        DocumentSerialSequenceEntity seq = sequenceRepository
                .findByDocTypeAndYearForUpdate(docType, year)
                .orElseGet(() -> {
                    // First document of this type in a new year — create the row.
                    DocumentSerialSequenceEntity newSeq = new DocumentSerialSequenceEntity();
                    newSeq.setDocType(docType);
                    newSeq.setYear(year);
                    newSeq.setLastSeq(0);
                    return sequenceRepository.save(newSeq);
                });

        int next = seq.getLastSeq() + 1;
        seq.setLastSeq(next);
        sequenceRepository.save(seq);

        return String.format("%s/%d/%06d", docType, year, next);
    }
}
