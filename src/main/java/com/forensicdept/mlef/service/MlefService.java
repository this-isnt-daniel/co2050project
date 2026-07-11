package com.forensicdept.mlef.service;

import com.forensicdept.casemanagement.entity.CaseEntity;
import com.forensicdept.casemanagement.repository.CaseRepository;
import com.forensicdept.exception.DuplicateResourceException;
import com.forensicdept.exception.ResourceNotFoundException;
import com.forensicdept.mlef.dto.MlefRequest;
import com.forensicdept.mlef.dto.MlefResponse;
import com.forensicdept.mlef.entity.MlefEntity;
import com.forensicdept.mlef.repository.MlefRepository;
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
public class MlefService {

    private final MlefRepository mlefRepository;
    private final CaseRepository caseRepository;
    private final StaffRepository staffRepository;

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO')")
    @Transactional(readOnly = true)
    public Page<MlefResponse> findAll(Long doctorId, String status, Pageable pageable) {
        if (doctorId != null) {
            return mlefRepository.findByExaminingDoctorId(doctorId, pageable).map(this::toResponse);
        }
        if (status != null) {
            return mlefRepository.findByReportStatus(status, pageable).map(this::toResponse);
        }
        return mlefRepository.findAll(pageable).map(this::toResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','CLERICAL')")
    @Transactional(readOnly = true)
    public MlefResponse findById(Long id) {
        return toResponse(mlefRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MLEF", id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO')")
    @Transactional
    public MlefResponse create(MlefRequest request) {
        if (mlefRepository.findByCaseRefId(request.getCaseId()).isPresent()) {
            throw new DuplicateResourceException("MLEF already exists for case id: " + request.getCaseId());
        }
        CaseEntity caseRef = caseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case", request.getCaseId()));
        StaffEntity doctor = staffRepository.findById(request.getExaminingDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff", request.getExaminingDoctorId()));

        MlefEntity entity = MlefEntity.builder()
                .caseRef(caseRef)
                .examiningDoctor(doctor)
                .dateOfIssue(request.getDateOfIssue())
                .examinationDateTime(request.getExaminationDateTime())
                .natureOfBodilyHarm(request.getNatureOfBodilyHarm())
                .causativeWeapon(request.getCausativeWeapon())
                .alcoholDrugTestResults(request.getAlcoholDrugTestResults())
                .findings(request.getFindings())
                .reportStatus(request.getReportStatus() != null ? request.getReportStatus() : "DRAFT")
                .build();
        return toResponse(mlefRepository.save(entity));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO')")
    @Transactional
    public MlefResponse update(Long id, MlefRequest request) {
        MlefEntity entity = mlefRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MLEF", id));
        entity.setDateOfIssue(request.getDateOfIssue());
        entity.setExaminationDateTime(request.getExaminationDateTime());
        entity.setNatureOfBodilyHarm(request.getNatureOfBodilyHarm());
        entity.setCausativeWeapon(request.getCausativeWeapon());
        entity.setAlcoholDrugTestResults(request.getAlcoholDrugTestResults());
        entity.setFindings(request.getFindings());
        if (request.getReportStatus() != null) {
            entity.setReportStatus(request.getReportStatus());
        }
        return toResponse(mlefRepository.save(entity));
    }

    @PreAuthorize("hasAnyRole('ADMIN','JMO')")
    @Transactional(readOnly = true)
    public List<MlefResponse> findOverdueDrafts(int thresholdDays) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(thresholdDays);
        return mlefRepository.findOverdueDraftMlef(threshold).stream().map(this::toResponse).toList();
    }

    private MlefResponse toResponse(MlefEntity e) {
        return MlefResponse.builder()
                .id(e.getId())
                .caseId(e.getCaseRef().getId())
                .caseNumber(e.getCaseRef().getCaseNumber())
                .examiningDoctorId(e.getExaminingDoctor().getId())
                .examiningDoctorName(e.getExaminingDoctor().getName())
                .dateOfIssue(e.getDateOfIssue())
                .examinationDateTime(e.getExaminationDateTime())
                .natureOfBodilyHarm(e.getNatureOfBodilyHarm())
                .causativeWeapon(e.getCausativeWeapon())
                .alcoholDrugTestResults(e.getAlcoholDrugTestResults())
                .findings(e.getFindings())
                .reportStatus(e.getReportStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
