package org.zakariafarih.syncly.repository;

import org.zakariafarih.syncly.model.PasteBin;
import org.zakariafarih.syncly.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasteBinRepository extends JpaRepository<PasteBin, Long> {
    Page<PasteBin> findByUserAndIsDeletedFalse(User user, Pageable pageable);
    Page<PasteBin> findByUserAndNameContainingAndIsDeletedFalse(User user, String searchTerm, Pageable pageable);
}
