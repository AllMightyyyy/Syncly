package org.zakariafarih.syncly.repository;

import org.zakariafarih.syncly.model.PasswordHistory;
import org.zakariafarih.syncly.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    List<PasswordHistory> findTop5ByUserOrderByChangedAtDesc(User user);
}