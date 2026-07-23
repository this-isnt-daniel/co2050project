package com.forensicdept.mlr.repository;

import com.forensicdept.mlr.entity.MlrEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MlrRepository extends JpaRepository<MlrEntity, Long> {

    Optional<MlrEntity> findByCaseRefId(Long caseId);

    Page<MlrEntity> findByReportStatus(String reportStatus, Pageable pageable);

    Page<MlrEntity> findByPreparedById(Long staffId, Pageable pageable);

    boolean existsByCaseRefId(Long caseId);
}
