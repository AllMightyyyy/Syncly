package org.zakariafarih.syncly.repository;

import org.zakariafarih.syncly.model.ClipboardEntry;
import org.zakariafarih.syncly.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClipboardEntryRepository extends JpaRepository<ClipboardEntry, Long> {
    Page<ClipboardEntry> findByUserAndIsDeletedFalse(User user, Pageable pageable);
    Page<ClipboardEntry> findByUserAndContentContainingAndIsDeletedFalse(User user, String searchTerm, Pageable pageable);
}
