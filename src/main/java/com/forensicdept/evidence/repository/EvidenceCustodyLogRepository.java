package com.forensicdept.evidence.repository;

import com.forensicdept.evidence.entity.EvidenceCustodyLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenceCustodyLogRepository extends JpaRepository<EvidenceCustodyLogEntity, Long> {

    List<EvidenceCustodyLogEntity> findByEvidenceIdOrderByTransferTimestampAsc(Long evidenceId);
}
