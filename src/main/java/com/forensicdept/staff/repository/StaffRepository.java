package com.forensicdept.staff.repository;

import com.forensicdept.staff.entity.StaffEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffRepository extends JpaRepository<StaffEntity, Long> {

    Page<StaffEntity> findByStaffRoleAndIsActiveTrue(String staffRole, Pageable pageable);

    Page<StaffEntity> findByIsActiveTrue(Pageable pageable);
}
