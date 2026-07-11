package com.forensicdept.postmortem.repository;

import com.forensicdept.postmortem.entity.PostmortemEntity;
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
public interface PostmortemRepository extends JpaRepository<PostmortemEntity, Long> {

    Optional<PostmortemEntity> findByCaseRefId(Long caseId);

    Page<PostmortemEntity> findByDoctorId(Long doctorId, Pageable pageable);

    Page<PostmortemEntity> findByCauseOfDeathCategory(String category, Pageable pageable);

    /** PM cases where cause of death is still null (pending finalisation). */
    @Query("SELECT p FROM PostmortemEntity p WHERE p.causeOfDeath IS NULL AND p.createdAt < :thresholdDate")
    List<PostmortemEntity> findPendingCauseOfDeath(@Param("thresholdDate") LocalDateTime thresholdDate);
}
