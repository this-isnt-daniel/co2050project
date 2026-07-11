package com.forensicdept.patient.service;

import com.forensicdept.exception.DuplicateResourceException;
import com.forensicdept.exception.ResourceNotFoundException;
import com.forensicdept.patient.dto.PatientDeidentifiedResponse;
import com.forensicdept.patient.dto.PatientRequest;
import com.forensicdept.patient.dto.PatientResponse;
import com.forensicdept.patient.entity.PatientEntity;
import com.forensicdept.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    /** Full patient list — accessible to all staff except RESEARCHER. */
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','LAB_STAFF','CLERICAL')")
    @Transactional(readOnly = true)
    public Page<PatientResponse> findAll(String name, String gender, Pageable pageable) {
        return patientRepository.search(name, gender, pageable).map(this::toResponse);
    }

    /** De-identified list for RESEARCHER — separate return type, not a runtime filter. */
    @PreAuthorize("hasRole('RESEARCHER')")
    @Transactional(readOnly = true)
    public Page<PatientDeidentifiedResponse> findAllDeidentified(Pageable pageable) {
        return patientRepository.findAll(pageable).map(this::toDeidentifiedResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','LAB_STAFF','CLERICAL')")
    @Transactional(readOnly = true)
    public PatientResponse findById(Long id) {
        return toResponse(patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','CLERICAL')")
    @Transactional
    public PatientResponse create(PatientRequest request) {
        if (request.getNicPassportNo() != null &&
            patientRepository.findByNicPassportNo(request.getNicPassportNo()).isPresent()) {
            throw new DuplicateResourceException("Patient with NIC/Passport already exists: " + request.getNicPassportNo());
        }
        PatientEntity entity = PatientEntity.builder()
                .fullName(request.getFullName())
                .age(request.getAge())
                .gender(request.getGender())
                .address(request.getAddress())
                .nicPassportNo(request.getNicPassportNo())
                .contactInfo(request.getContactInfo())
                .build();
        return toResponse(patientRepository.save(entity));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','CLERICAL')")
    @Transactional
    public PatientResponse update(Long id, PatientRequest request) {
        PatientEntity entity = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
        entity.setFullName(request.getFullName());
        entity.setAge(request.getAge());
        entity.setGender(request.getGender());
        entity.setAddress(request.getAddress());
        entity.setNicPassportNo(request.getNicPassportNo());
        entity.setContactInfo(request.getContactInfo());
        return toResponse(patientRepository.save(entity));
    }

    private PatientResponse toResponse(PatientEntity e) {
        return PatientResponse.builder()
                .id(e.getId())
                .fullName(e.getFullName())
                .age(e.getAge())
                .gender(e.getGender())
                .address(e.getAddress())
                .nicPassportNo(e.getNicPassportNo())
                .contactInfo(e.getContactInfo())
                .build();
    }

    private PatientDeidentifiedResponse toDeidentifiedResponse(PatientEntity e) {
        return PatientDeidentifiedResponse.builder()
                .id(e.getId())
                .age(e.getAge())
                .gender(e.getGender())
                .build();
    }
}
