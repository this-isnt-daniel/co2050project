package com.forensicdept.casemanagement.repository;

import com.forensicdept.casemanagement.entity.CaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseRepository extends JpaRepository<CaseEntity, Long> {

    boolean existsByCaseNumber(String caseNumber);

    Page<CaseEntity> findByAssignedDoctorId(Long doctorId, Pageable pageable);

    Page<CaseEntity> findByCaseStatus(String status, Pageable pageable);

    Page<CaseEntity> findByCaseType(String caseType, Pageable pageable);

    @Query("SELECT c FROM CaseEntity c WHERE " +
           "(:status IS NULL OR c.caseStatus = :status) AND " +
           "(:caseType IS NULL OR c.caseType = :caseType) AND " +
           "(:doctorId IS NULL OR c.assignedDoctor.id = :doctorId)")
    Page<CaseEntity> search(@Param("status") String status,
                            @Param("caseType") String caseType,
                            @Param("doctorId") Long doctorId,
                            Pageable pageable);
}
