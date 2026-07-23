package com.forensicdept.courtreport.service;

import com.forensicdept.casemanagement.entity.CaseEntity;
import com.forensicdept.casemanagement.repository.CaseRepository;
import com.forensicdept.common.service.SerialNumberService;
import com.forensicdept.courtreport.dto.CourtReportRequest;
import com.forensicdept.courtreport.dto.CourtReportResponse;
import com.forensicdept.courtreport.entity.CourtReportEntity;
import com.forensicdept.courtreport.repository.CourtReportRepository;
import com.forensicdept.exception.ResourceNotFoundException;
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
public class CourtReportService {

    private final CourtReportRepository courtReportRepository;
    private final CaseRepository caseRepository;
    private final StaffRepository staffRepository;
    private final SerialNumberService serialNumberService;

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','CLERICAL')")
    @Transactional(readOnly = true)
    public Page<CourtReportResponse> findAll(String status, Pageable pageable) {
        if (status != null) {
            return courtReportRepository.findByReportStatus(status, pageable).map(this::toResponse);
        }
        return courtReportRepository.findAll(pageable).map(this::toResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','CLERICAL')")
    @Transactional(readOnly = true)
    public CourtReportResponse findById(Long id) {
        return toResponse(courtReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CourtReport", id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','JMO','CLERICAL')")
    @Transactional
    public CourtReportResponse create(CourtReportRequest request) {
        CaseEntity caseRef = caseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case", request.getCaseId()));

        StaffEntity preparedBy = null;
        if (request.getPreparedById() != null) {
            preparedBy = staffRepository.findById(request.getPreparedById())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff", request.getPreparedById()));
        }

        String courtReportNumber = serialNumberService.nextSerial("CRT");

        CourtReportEntity entity = CourtReportEntity.builder()
                .courtReportNumber(courtReportNumber)
                .caseRef(caseRef)
                .reportType(request.getReportType())
                .submissionDate(request.getSubmissionDate())
                .requestedDate(request.getRequestedDate())
                .reportStatus(request.getReportStatus() != null ? request.getReportStatus() : "DRAFT")
                .courtName(request.getCourtName())
                .courtCaseNumber(request.getCourtCaseNumber())
                .dateOfTrial(request.getDateOfTrial())
                .certificateOfReceiptRef(request.getCertificateOfReceiptRef())
                .preparedBy(preparedBy)
                .build();
        return toResponse(courtReportRepository.save(entity));
    }

    @PreAuthorize("hasAnyRole('ADMIN','JMO','CLERICAL')")
    @Transactional
    public CourtReportResponse update(Long id, CourtReportRequest request) {
        CourtReportEntity entity = courtReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CourtReport", id));

        if (request.getPreparedById() != null) {
            StaffEntity preparedBy = staffRepository.findById(request.getPreparedById())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff", request.getPreparedById()));
            entity.setPreparedBy(preparedBy);
        }

        entity.setSubmissionDate(request.getSubmissionDate());
        entity.setRequestedDate(request.getRequestedDate());
        entity.setReportStatus(request.getReportStatus() != null ? request.getReportStatus() : entity.getReportStatus());
        entity.setCourtName(request.getCourtName());
        entity.setCourtCaseNumber(request.getCourtCaseNumber());
        entity.setDateOfTrial(request.getDateOfTrial());
        entity.setCertificateOfReceiptRef(request.getCertificateOfReceiptRef());
        return toResponse(courtReportRepository.save(entity));
    }

    @PreAuthorize("hasAnyRole('ADMIN','JMO')")
    @Transactional(readOnly = true)
    public List<CourtReportResponse> findUpcomingTrials(int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(daysAhead);
        return courtReportRepository.findUpcomingTrials(today, cutoff).stream().map(this::toResponse).toList();
    }

    private CourtReportResponse toResponse(CourtReportEntity e) {
        return CourtReportResponse.builder()
                .courtReportNumber(e.getCourtReportNumber())
                .id(e.getId())
                .caseId(e.getCaseRef().getId())
                .caseNumber(e.getCaseRef().getCaseNumber())
                .reportType(e.getReportType())
                .submissionDate(e.getSubmissionDate())
                .requestedDate(e.getRequestedDate())
                .reportStatus(e.getReportStatus())
                .courtName(e.getCourtName())
                .courtCaseNumber(e.getCourtCaseNumber())
                .dateOfTrial(e.getDateOfTrial())
                .certificateOfReceiptRef(e.getCertificateOfReceiptRef())
                .preparedById(e.getPreparedBy() != null ? e.getPreparedBy().getId() : null)
                .preparedByName(e.getPreparedBy() != null ? e.getPreparedBy().getName() : null)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
