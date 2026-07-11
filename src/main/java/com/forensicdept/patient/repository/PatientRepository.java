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
           "(:name IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:gender IS NULL OR p.gender = :gender)")
    Page<PatientEntity> search(@Param("name") String name,
                               @Param("gender") String gender,
                               Pageable pageable);
}
