package com.forensicdept.evidence.service;

import com.forensicdept.casemanagement.entity.CaseEntity;
import com.forensicdept.casemanagement.repository.CaseRepository;
import com.forensicdept.evidence.dto.*;
import com.forensicdept.evidence.entity.EvidenceCustodyLogEntity;
import com.forensicdept.evidence.entity.EvidenceEntity;
import com.forensicdept.evidence.repository.EvidenceCustodyLogRepository;
import com.forensicdept.evidence.repository.EvidenceRepository;
import com.forensicdept.exception.ResourceNotFoundException;
import com.forensicdept.staff.entity.StaffEntity;
import com.forensicdept.staff.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final EvidenceCustodyLogRepository custodyLogRepository;
    private final CaseRepository caseRepository;
    private final StaffRepository staffRepository;

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','LAB_STAFF')")
    @Transactional(readOnly = true)
    public Page<EvidenceResponse> findByCaseId(Long caseId, Pageable pageable) {
        return evidenceRepository.findByCaseRefId(caseId, pageable).map(this::toResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','LAB_STAFF')")
    @Transactional(readOnly = true)
    public EvidenceResponse findById(Long id) {
        return toResponse(evidenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence", id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','JMO','LAB_STAFF')")
    @Transactional
    public EvidenceResponse create(EvidenceRequest request) {
        CaseEntity caseRef = caseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case", request.getCaseId()));
        StaffEntity collector = null;
        if (request.getCollectedById() != null) {
            collector = staffRepository.findById(request.getCollectedById())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff", request.getCollectedById()));
        }
        EvidenceEntity entity = EvidenceEntity.builder()
                .caseRef(caseRef)
                .evidenceType(request.getEvidenceType())
                .description(request.getDescription())
                .storageLocation(request.getStorageLocation())
                .collectedBy(collector)
                .collectedAt(request.getCollectedAt())
                .build();

        EvidenceEntity saved = evidenceRepository.save(entity);

        // Log initial custody entry
        EvidenceCustodyLogEntity firstEntry = EvidenceCustodyLogEntity.builder()
                .evidence(saved)
                .transferredFrom(null)
                .transferredTo(collector)
                .transferTimestamp(request.getCollectedAt() != null ? request.getCollectedAt() : LocalDateTime.now())
                .reason("Initial collection")
                .build();
        custodyLogRepository.save(firstEntry);

        return toResponse(evidenceRepository.findById(saved.getId()).orElseThrow());
    }

    /** Records a chain-of-custody transfer. */
    @PreAuthorize("hasAnyRole('ADMIN','JMO','LAB_STAFF')")
    @Transactional
    public CustodyLogResponse recordTransfer(CustodyTransferRequest request) {
        EvidenceEntity evidence = evidenceRepository.findById(request.getEvidenceId())
                .orElseThrow(() -> new ResourceNotFoundException("Evidence", request.getEvidenceId()));
        StaffEntity from = null;
        if (request.getTransferredFromId() != null) {
            from = staffRepository.findById(request.getTransferredFromId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff", request.getTransferredFromId()));
        }
        StaffEntity to = staffRepository.findById(request.getTransferredToId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff", request.getTransferredToId()));

        EvidenceCustodyLogEntity log = EvidenceCustodyLogEntity.builder()
                .evidence(evidence)
                .transferredFrom(from)
                .transferredTo(to)
                .transferTimestamp(request.getTransferTimestamp() != null ? request.getTransferTimestamp() : LocalDateTime.now())
                .reason(request.getReason())
                .build();
        EvidenceCustodyLogEntity saved = custodyLogRepository.save(log);
        return toCustodyResponse(saved);
    }

    private EvidenceResponse toResponse(EvidenceEntity e) {
        List<CustodyLogResponse> logs = custodyLogRepository
                .findByEvidenceIdOrderByTransferTimestampAsc(e.getId())
                .stream().map(this::toCustodyResponse).toList();

        return EvidenceResponse.builder()
                .id(e.getId())
                .caseId(e.getCaseRef().getId())
                .evidenceType(e.getEvidenceType())
                .description(e.getDescription())
                .storageLocation(e.getStorageLocation())
                .collectedById(e.getCollectedBy() != null ? e.getCollectedBy().getId() : null)
                .collectedByName(e.getCollectedBy() != null ? e.getCollectedBy().getName() : null)
                .collectedAt(e.getCollectedAt())
                .custodyLog(logs)
                .build();
    }

    private CustodyLogResponse toCustodyResponse(EvidenceCustodyLogEntity l) {
        return CustodyLogResponse.builder()
                .id(l.getId())
                .transferredFromId(l.getTransferredFrom() != null ? l.getTransferredFrom().getId() : null)
                .transferredFromName(l.getTransferredFrom() != null ? l.getTransferredFrom().getName() : null)
                .transferredToId(l.getTransferredTo() != null ? l.getTransferredTo().getId() : null)
                .transferredToName(l.getTransferredTo() != null ? l.getTransferredTo().getName() : null)
                .transferTimestamp(l.getTransferTimestamp())
                .reason(l.getReason())
                .build();
    }
}
