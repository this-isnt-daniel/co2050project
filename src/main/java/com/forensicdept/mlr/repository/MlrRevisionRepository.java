package com.forensicdept.mlr.repository;

import com.forensicdept.mlr.entity.MlrRevisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MlrRevisionRepository extends JpaRepository<MlrRevisionEntity, Long> {

    List<MlrRevisionEntity> findByMlrIdOrderByRevisionNumberAsc(Long mlrId);

    @Query("SELECT COALESCE(MAX(r.revisionNumber), 0) FROM MlrRevisionEntity r WHERE r.mlr.id = :mlrId")
    Integer findMaxRevisionNumber(@Param("mlrId") Long mlrId);
}
