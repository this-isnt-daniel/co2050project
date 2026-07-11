package com.forensicdept.casemanagement.service;

import com.forensicdept.casemanagement.dto.CaseRequest;
import com.forensicdept.casemanagement.dto.CaseResponse;
import com.forensicdept.casemanagement.entity.CaseEntity;
import com.forensicdept.casemanagement.repository.CaseRepository;
import com.forensicdept.exception.DuplicateResourceException;
import com.forensicdept.exception.ResourceNotFoundException;
import com.forensicdept.patient.entity.PatientEntity;
import com.forensicdept.patient.repository.PatientRepository;
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
public class CaseService {

    private final CaseRepository caseRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;

    /** ADMIN and JMO can see all cases; DOCTOR sees all via this method but filtered in controller. */
    @PreAuthorize("hasAnyRole('ADMIN','JMO','CLERICAL','LAB_STAFF')")
    @Transactional(readOnly = true)
    public Page<CaseResponse> findAll(String status, String caseType, Long doctorId, Pageable pageable) {
        return caseRepository.search(status, caseType, doctorId, pageable).map(this::toResponse);
    }

    /** Doctors can only query their own cases (enforced via doctorId param). */
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','CLERICAL','LAB_STAFF')")
    @Transactional(readOnly = true)
    public Page<CaseResponse> findByDoctor(Long doctorId, Pageable pageable) {
        return caseRepository.findByAssignedDoctorId(doctorId, pageable).map(this::toResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','CLERICAL','LAB_STAFF')")
    @Transactional(readOnly = true)
    public CaseResponse findById(Long id) {
        return toResponse(caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case", id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','JMO','CLERICAL')")
    @Transactional
    public CaseResponse create(CaseRequest request) {
        if (caseRepository.existsByCaseNumber(request.getCaseNumber())) {
            throw new DuplicateResourceException("Case number already exists: " + request.getCaseNumber());
        }
        PatientEntity patient = null;
        if (request.getPatientId() != null) {
            patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient", request.getPatientId()));
        }
        StaffEntity doctor = null;
        if (request.getAssignedDoctorId() != null) {
            doctor = staffRepository.findById(request.getAssignedDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff", request.getAssignedDoctorId()));
        }
        CaseEntity entity = CaseEntity.builder()
                .caseNumber(request.getCaseNumber())
                .caseType(request.getCaseType())
                .patient(patient)
                .incidentDate(request.getIncidentDate())
                .referringAuthority(request.getReferringAuthority())
                .referredBy(request.getReferredBy())
                .caseStatus("OPEN")
                .assignedDoctor(doctor)
                .build();
        return toResponse(caseRepository.save(entity));
    }

    @PreAuthorize("hasAnyRole('ADMIN','JMO','CLERICAL')")
    @Transactional
    public CaseResponse update(Long id, CaseRequest request) {
        CaseEntity entity = caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case", id));
        if (!entity.getCaseNumber().equals(request.getCaseNumber()) &&
            caseRepository.existsByCaseNumber(request.getCaseNumber())) {
            throw new DuplicateResourceException("Case number already exists: " + request.getCaseNumber());
        }
        entity.setCaseNumber(request.getCaseNumber());
        entity.setIncidentDate(request.getIncidentDate());
        entity.setReferringAuthority(request.getReferringAuthority());
        entity.setReferredBy(request.getReferredBy());
        if (request.getAssignedDoctorId() != null) {
            StaffEntity doctor = staffRepository.findById(request.getAssignedDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff", request.getAssignedDoctorId()));
            entity.setAssignedDoctor(doctor);
        }
        return toResponse(caseRepository.save(entity));
    }

    @PreAuthorize("hasAnyRole('ADMIN','JMO')")
    @Transactional
    public CaseResponse updateStatus(Long id, String newStatus) {
        CaseEntity entity = caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case", id));
        entity.setCaseStatus(newStatus);
        return toResponse(caseRepository.save(entity));
    }

    private CaseResponse toResponse(CaseEntity e) {
        return CaseResponse.builder()
                .id(e.getId())
                .caseNumber(e.getCaseNumber())
                .caseType(e.getCaseType())
                .patientId(e.getPatient() != null ? e.getPatient().getId() : null)
                .patientName(e.getPatient() != null ? e.getPatient().getFullName() : null)
                .incidentDate(e.getIncidentDate())
                .referringAuthority(e.getReferringAuthority())
                .referredBy(e.getReferredBy())
                .caseStatus(e.getCaseStatus())
                .assignedDoctorId(e.getAssignedDoctor() != null ? e.getAssignedDoctor().getId() : null)
                .assignedDoctorName(e.getAssignedDoctor() != null ? e.getAssignedDoctor().getName() : null)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
