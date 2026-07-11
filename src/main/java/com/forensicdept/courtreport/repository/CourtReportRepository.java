package com.forensicdept.courtreport.repository;

import com.forensicdept.courtreport.entity.CourtReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CourtReportRepository extends JpaRepository<CourtReportEntity, Long> {

    Page<CourtReportEntity> findByCaseRefId(Long caseId, Pageable pageable);

    Page<CourtReportEntity> findByReportStatus(String status, Pageable pageable);

    /** Reports with upcoming trial dates within N days (for notification scheduler). */
    @Query("SELECT cr FROM CourtReportEntity cr WHERE cr.dateOfTrial BETWEEN :today AND :cutoff")
    List<CourtReportEntity> findUpcomingTrials(@Param("today") LocalDate today,
                                               @Param("cutoff") LocalDate cutoff);
}
