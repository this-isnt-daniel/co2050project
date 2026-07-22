package com.forensicdept.patient.repository;

import com.forensicdept.patient.entity.PatientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<PatientEntity, Long> {

    Optional<PatientEntity> findByNicPassportNo(String nicPassportNo);

    Page<PatientEntity> findByFullNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT p FROM PatientEntity p WHERE " +
           "(CAST(:name AS string) IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) AND " +
           "(CAST(:gender AS string) IS NULL OR p.gender = CAST(:gender AS string))")
    Page<PatientEntity> search(@Param("name") String name,
                               @Param("gender") String gender,
                               Pageable pageable);
}
