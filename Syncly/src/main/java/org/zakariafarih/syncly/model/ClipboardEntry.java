package org.zakariafarih.syncly.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity class representing a clipboard entry.
 * Maps to the "clipboard_entries" table in the database.
 */
@Entity
@Table(name = "clipboard_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClipboardEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // Encrypted

    @Column
    private String category;

    @ElementCollection
    @CollectionTable(name = "clipboard_tags", joinColumns = @JoinColumn(name = "clipboard_entry_id"))
    @Column(name = "tag")
    private List<String> tags;

    @CreationTimestamp
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String deviceInfo;

    @Column(nullable = false)
    private Boolean isDeleted = false;
}