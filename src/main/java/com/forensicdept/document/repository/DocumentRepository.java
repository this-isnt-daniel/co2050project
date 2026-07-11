package com.forensicdept.document.repository;

import com.forensicdept.document.entity.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

    Page<DocumentEntity> findByOwnerTypeAndOwnerId(String ownerType, Long ownerId, Pageable pageable);
}
