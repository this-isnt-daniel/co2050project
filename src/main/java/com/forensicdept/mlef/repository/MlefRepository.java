package com.forensicdept.mlef.repository;

import com.forensicdept.mlef.entity.MlefEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MlefRepository extends JpaRepository<MlefEntity, Long> {

    Optional<MlefEntity> findByCaseRefId(Long caseId);

    Page<MlefEntity> findByExaminingDoctorId(Long doctorId, Pageable pageable);

    Page<MlefEntity> findByReportStatus(String reportStatus, Pageable pageable);

    /** Cases where the MLEF is still DRAFT past a threshold date (for notification job). */
    @Query("SELECT m FROM MlefEntity m WHERE m.reportStatus = 'DRAFT' AND m.createdAt < :thresholdDate")
    List<MlefEntity> findOverdueDraftMlef(@Param("thresholdDate") LocalDateTime thresholdDate);
}
