package com.forensicdept.staff.service;

import com.forensicdept.exception.ResourceNotFoundException;
import com.forensicdept.staff.dto.StaffRequest;
import com.forensicdept.staff.dto.StaffResponse;
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
public class StaffService {

    private final StaffRepository staffRepository;

    @PreAuthorize("hasAnyRole('ADMIN','JMO','CLERICAL')")
    @Transactional(readOnly = true)
    public Page<StaffResponse> findAll(String role, Pageable pageable) {
        if (role != null) {
            return staffRepository.findByStaffRoleAndIsActiveTrue(role, pageable).map(this::toResponse);
        }
        return staffRepository.findByIsActiveTrue(pageable).map(this::toResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','LAB_STAFF','CLERICAL')")
    @Transactional(readOnly = true)
    public StaffResponse findById(Long id) {
        return toResponse(staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public StaffResponse create(StaffRequest request) {
        StaffEntity entity = StaffEntity.builder()
                .name(request.getName())
                .staffRole(request.getStaffRole())
                .contactNo(request.getContactNo())
                .specialization(request.getSpecialization())
                .build();
        return toResponse(staffRepository.save(entity));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public StaffResponse update(Long id, StaffRequest request) {
        StaffEntity entity = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id));
        entity.setName(request.getName());
        entity.setStaffRole(request.getStaffRole());
        entity.setContactNo(request.getContactNo());
        entity.setSpecialization(request.getSpecialization());
        return toResponse(staffRepository.save(entity));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deactivate(Long id) {
        StaffEntity entity = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id));
        entity.setIsActive(false);
        staffRepository.save(entity);
    }

    private StaffResponse toResponse(StaffEntity e) {
        return StaffResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .staffRole(e.getStaffRole())
                .contactNo(e.getContactNo())
                .specialization(e.getSpecialization())
                .isActive(e.getIsActive())
                .build();
    }
}
