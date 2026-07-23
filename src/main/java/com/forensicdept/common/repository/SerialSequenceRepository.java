package com.forensicdept.common.repository;

import com.forensicdept.common.entity.DocumentSerialSequenceEntity;
import com.forensicdept.common.entity.DocumentSerialSequenceId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SerialSequenceRepository
        extends JpaRepository<DocumentSerialSequenceEntity, DocumentSerialSequenceId> {

    /**
     * Pessimistic write lock to prevent concurrent serial number collisions.
     * Translates to SELECT ... FOR UPDATE in PostgreSQL.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DocumentSerialSequenceEntity s WHERE s.docType = :docType AND s.year = :year")
    Optional<DocumentSerialSequenceEntity> findByDocTypeAndYearForUpdate(
            @Param("docType") String docType,
            @Param("year") Integer year);
}
