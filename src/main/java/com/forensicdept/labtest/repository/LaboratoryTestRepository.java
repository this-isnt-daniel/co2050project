package com.forensicdept.labtest.repository;

import com.forensicdept.labtest.entity.LaboratoryTestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LaboratoryTestRepository extends JpaRepository<LaboratoryTestEntity, Long> {

    Page<LaboratoryTestEntity> findByCaseRefId(Long caseId, Pageable pageable);

    Page<LaboratoryTestEntity> findByResultIsNull(Pageable pageable);
}
