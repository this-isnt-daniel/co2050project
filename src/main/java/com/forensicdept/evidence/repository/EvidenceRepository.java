package com.forensicdept.evidence.repository;

import com.forensicdept.evidence.entity.EvidenceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvidenceRepository extends JpaRepository<EvidenceEntity, Long> {

    Page<EvidenceEntity> findByCaseRefId(Long caseId, Pageable pageable);
}
