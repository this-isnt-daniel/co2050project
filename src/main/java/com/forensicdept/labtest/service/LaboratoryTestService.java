package com.forensicdept.labtest.service;

import com.forensicdept.casemanagement.entity.CaseEntity;
import com.forensicdept.casemanagement.repository.CaseRepository;
import com.forensicdept.exception.ResourceNotFoundException;
import com.forensicdept.labtest.dto.LaboratoryTestRequest;
import com.forensicdept.labtest.dto.LaboratoryTestResponse;
import com.forensicdept.labtest.entity.LaboratoryTestEntity;
import com.forensicdept.labtest.repository.LaboratoryTestRepository;
import com.forensicdept.staff.entity.StaffEntity;
import com.forensicdept.staff.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LaboratoryTestService {

    private final LaboratoryTestRepository labTestRepository;
    private final CaseRepository caseRepository;
    private final StaffRepository staffRepository;

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','LAB_STAFF')")
    @Transactional(readOnly = true)
    public Page<LaboratoryTestResponse> findByCaseId(Long caseId, Pageable pageable) {
        return labTestRepository.findByCaseRefId(caseId, pageable).map(this::toResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','LAB_STAFF')")
    @Transactional(readOnly = true)
    public LaboratoryTestResponse findById(Long id) {
        return toResponse(labTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LaboratoryTest", id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','LAB_STAFF')")
    @Transactional
    public LaboratoryTestResponse create(LaboratoryTestRequest request) {
        CaseEntity caseRef = caseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case", request.getCaseId()));
        StaffEntity requestedBy = null;
        if (request.getRequestedById() != null) {
            requestedBy = staffRepository.findById(request.getRequestedById())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff", request.getRequestedById()));
        }
        LaboratoryTestEntity entity = LaboratoryTestEntity.builder()
                .caseRef(caseRef)
                .testType(request.getTestType())
                .requestedBy(requestedBy)
                .result(request.getResult())
                .resultDate(request.getResultDate())
                .build();
        return toResponse(labTestRepository.save(entity));
    }

    @PreAuthorize("hasAnyRole('ADMIN','LAB_STAFF')")
    @Transactional
    public LaboratoryTestResponse updateResult(Long id, String result, java.time.LocalDate resultDate) {
        LaboratoryTestEntity entity = labTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LaboratoryTest", id));
        entity.setResult(result);
        entity.setResultDate(resultDate);
        return toResponse(labTestRepository.save(entity));
    }

    private LaboratoryTestResponse toResponse(LaboratoryTestEntity e) {
        return LaboratoryTestResponse.builder()
                .id(e.getId())
                .caseId(e.getCaseRef().getId())
                .caseNumber(e.getCaseRef().getCaseNumber())
                .testType(e.getTestType())
                .requestedById(e.getRequestedBy() != null ? e.getRequestedBy().getId() : null)
                .requestedByName(e.getRequestedBy() != null ? e.getRequestedBy().getName() : null)
                .result(e.getResult())
                .resultDate(e.getResultDate())
                .build();
    }
}
