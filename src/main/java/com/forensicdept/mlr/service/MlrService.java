package com.forensicdept.mlr.service;

import com.forensicdept.casemanagement.entity.CaseEntity;
import com.forensicdept.casemanagement.repository.CaseRepository;
import com.forensicdept.common.service.SerialNumberService;
import com.forensicdept.exception.DuplicateResourceException;
import com.forensicdept.exception.ResourceNotFoundException;
import com.forensicdept.mlr.dto.MlrRequest;
import com.forensicdept.mlr.dto.MlrResponse;
import com.forensicdept.mlr.dto.MlrRevisionResponse;
import com.forensicdept.mlr.entity.MlrEntity;
import com.forensicdept.mlr.entity.MlrRevisionEntity;
import com.forensicdept.mlr.repository.MlrRepository;
import com.forensicdept.mlr.repository.MlrRevisionRepository;
import com.forensicdept.staff.entity.StaffEntity;
import com.forensicdept.staff.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MlrService {

    private final MlrRepository mlrRepository;
    private final MlrRevisionRepository revisionRepository;
    private final CaseRepository caseRepository;
    private final StaffRepository staffRepository;
    private final SerialNumberService serialNumberService;

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO')")
    @Transactional(readOnly = true)
    public Page<MlrResponse> findAll(Long preparedById, String status, Pageable pageable) {
        if (preparedById != null) {
            return mlrRepository.findByPreparedById(preparedById, pageable).map(this::toResponse);
        }
        if (status != null) {
            return mlrRepository.findByReportStatus(status, pageable).map(this::toResponse);
        }
        return mlrRepository.findAll(pageable).map(this::toResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','CLERICAL')")
    @Transactional(readOnly = true)
    public MlrResponse findById(Long id) {
        return toResponse(mlrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MLR", id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','CLERICAL')")
    @Transactional(readOnly = true)
    public MlrResponse findByCaseId(Long caseId) {
        return toResponse(mlrRepository.findByCaseRefId(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("MLR for case", caseId)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO')")
    @Transactional
    public MlrResponse create(MlrRequest request) {
        if (mlrRepository.existsByCaseRefId(request.getCaseId())) {
            throw new DuplicateResourceException("MLR already exists for case id: " + request.getCaseId());
        }
        CaseEntity caseRef = caseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case", request.getCaseId()));
        StaffEntity preparedBy = staffRepository.findById(request.getPreparedById())
                .orElseThrow(() -> new ResourceNotFoundException("Staff", request.getPreparedById()));

        String mlrNumber = serialNumberService.nextSerial("MLR");

        MlrEntity entity = MlrEntity.builder()
                .mlrNumber(mlrNumber)
                .caseRef(caseRef)
                .preparedBy(preparedBy)
                .examinationDate(request.getExaminationDate())
                .digitalReportPath(request.getDigitalReportPath())
                .reportStatus(request.getReportStatus() != null ? request.getReportStatus() : "DRAFT")
                .build();

        return toResponse(mlrRepository.save(entity));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO')")
    @Transactional
    public MlrResponse update(Long id, MlrRequest request, String revisionReason, Long revisedById) {
        MlrEntity entity = mlrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MLR", id));

        if ("FINALIZED".equals(entity.getReportStatus())) {
            throw new IllegalStateException("Finalized MLR cannot be directly modified. Create a revision instead.");
        }

        StaffEntity revisedBy = null;
        if (revisedById != null) {
            revisedBy = staffRepository.findById(revisedById)
                    .orElseThrow(() -> new ResourceNotFoundException("Staff", revisedById));
        }

        // Snapshot current version to revision history before applying modifications
        saveRevisionSnapshot(entity, revisionReason, revisedBy);

        if (request.getExaminationDate() != null) {
            entity.setExaminationDate(request.getExaminationDate());
        }
        if (request.getDigitalReportPath() != null) {
            entity.setDigitalReportPath(request.getDigitalReportPath());
        }
        if (request.getPreparedById() != null) {
            StaffEntity preparedBy = staffRepository.findById(request.getPreparedById())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff", request.getPreparedById()));
            entity.setPreparedBy(preparedBy);
        }

        return toResponse(mlrRepository.save(entity));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO')")
    @Transactional
    public MlrResponse finalizeReport(Long id) {
        MlrEntity entity = mlrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MLR", id));

        if ("FINALIZED".equals(entity.getReportStatus())) {
            throw new IllegalStateException("MLR is already finalized.");
        }

        entity.setReportStatus("FINALIZED");
        entity.setDateFinalized(LocalDate.now());

        return toResponse(mlrRepository.save(entity));
    }

    private void saveRevisionSnapshot(MlrEntity mlr, String reason, StaffEntity revisedBy) {
        int nextRevNum = revisionRepository.findMaxRevisionNumber(mlr.getId()) + 1;
        MlrRevisionEntity revision = MlrRevisionEntity.builder()
                .mlr(mlr)
                .revisionNumber(nextRevNum)
                .reportStatusAtRevision(mlr.getReportStatus())
                .digitalReportPath(mlr.getDigitalReportPath())
                .revisedBy(revisedBy)
                .revisionReason(reason != null ? reason : "Pre-update revision snapshot")
                .build();
        revisionRepository.save(revision);
    }

    private MlrResponse toResponse(MlrEntity e) {
        List<MlrRevisionResponse> revisions = revisionRepository
                .findByMlrIdOrderByRevisionNumberAsc(e.getId())
                .stream()
                .map(this::toRevisionResponse)
                .toList();

        return MlrResponse.builder()
                .mlrNumber(e.getMlrNumber())
                .id(e.getId())
                .caseId(e.getCaseRef().getId())
                .caseNumber(e.getCaseRef().getCaseNumber())
                .preparedById(e.getPreparedBy().getId())
                .preparedByName(e.getPreparedBy().getName())
                .examinationDate(e.getExaminationDate())
                .dateFinalized(e.getDateFinalized())
                .reportStatus(e.getReportStatus())
                .digitalReportPath(e.getDigitalReportPath())
                .revisions(revisions)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private MlrRevisionResponse toRevisionResponse(MlrRevisionEntity r) {
        return MlrRevisionResponse.builder()
                .id(r.getId())
                .revisionNumber(r.getRevisionNumber())
                .reportStatusAtRevision(r.getReportStatusAtRevision())
                .digitalReportPath(r.getDigitalReportPath())
                .revisedById(r.getRevisedBy() != null ? r.getRevisedBy().getId() : null)
                .revisedByName(r.getRevisedBy() != null ? r.getRevisedBy().getName() : null)
                .revisionReason(r.getRevisionReason())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
