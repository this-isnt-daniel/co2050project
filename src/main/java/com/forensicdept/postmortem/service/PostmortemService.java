package com.forensicdept.postmortem.service;

import com.forensicdept.casemanagement.entity.CaseEntity;
import com.forensicdept.casemanagement.repository.CaseRepository;
import com.forensicdept.exception.DuplicateResourceException;
import com.forensicdept.exception.ResourceNotFoundException;
import com.forensicdept.postmortem.dto.PostmortemRequest;
import com.forensicdept.postmortem.dto.PostmortemResponse;
import com.forensicdept.postmortem.entity.PostmortemEntity;
import com.forensicdept.postmortem.repository.PostmortemRepository;
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
public class PostmortemService {

    private final PostmortemRepository postmortemRepository;
    private final CaseRepository caseRepository;
    private final StaffRepository staffRepository;

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO')")
    @Transactional(readOnly = true)
    public Page<PostmortemResponse> findAll(Long doctorId, String category, Pageable pageable) {
        if (doctorId != null) {
            return postmortemRepository.findByDoctorId(doctorId, pageable).map(this::toResponse);
        }
        if (category != null) {
            return postmortemRepository.findByCauseOfDeathCategory(category, pageable).map(this::toResponse);
        }
        return postmortemRepository.findAll(pageable).map(this::toResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO','CLERICAL')")
    @Transactional(readOnly = true)
    public PostmortemResponse findById(Long id) {
        return toResponse(postmortemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Postmortem", id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO')")
    @Transactional
    public PostmortemResponse create(PostmortemRequest request) {
        if (postmortemRepository.findByCaseRefId(request.getCaseId()).isPresent()) {
            throw new DuplicateResourceException("Postmortem record already exists for case id: " + request.getCaseId());
        }
        CaseEntity caseRef = caseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case", request.getCaseId()));
        StaffEntity doctor = staffRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff", request.getDoctorId()));

        PostmortemEntity entity = PostmortemEntity.builder()
                .caseRef(caseRef).doctor(doctor)
                .inquestOrderRef(request.getInquestOrderRef())
                .inquestDate(request.getInquestDate())
                .placeOfPm(request.getPlaceOfPm())
                .causeOfDeathCategory(request.getCauseOfDeathCategory())
                .findings(request.getFindings())
                .causeOfDeath(request.getCauseOfDeath())
                .build();
        return toResponse(postmortemRepository.save(entity));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','JMO')")
    @Transactional
    public PostmortemResponse update(Long id, PostmortemRequest request) {
        PostmortemEntity entity = postmortemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Postmortem", id));
        entity.setInquestOrderRef(request.getInquestOrderRef());
        entity.setInquestDate(request.getInquestDate());
        entity.setPlaceOfPm(request.getPlaceOfPm());
        entity.setCauseOfDeathCategory(request.getCauseOfDeathCategory());
        entity.setFindings(request.getFindings());
        entity.setCauseOfDeath(request.getCauseOfDeath());
        return toResponse(postmortemRepository.save(entity));
    }

    @PreAuthorize("hasAnyRole('ADMIN','JMO')")
    @Transactional(readOnly = true)
    public List<PostmortemResponse> findPendingCauseOfDeath(int thresholdDays) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(thresholdDays);
        return postmortemRepository.findPendingCauseOfDeath(threshold).stream().map(this::toResponse).toList();
    }

    private PostmortemResponse toResponse(PostmortemEntity e) {
        return PostmortemResponse.builder()
                .id(e.getId())
                .caseId(e.getCaseRef().getId())
                .caseNumber(e.getCaseRef().getCaseNumber())
                .doctorId(e.getDoctor().getId())
                .doctorName(e.getDoctor().getName())
                .inquestOrderRef(e.getInquestOrderRef())
                .inquestDate(e.getInquestDate())
                .placeOfPm(e.getPlaceOfPm())
                .causeOfDeathCategory(e.getCauseOfDeathCategory())
                .findings(e.getFindings())
                .causeOfDeath(e.getCauseOfDeath())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
