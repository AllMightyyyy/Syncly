package org.zakariafarih.syncly.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.zakariafarih.syncly.model.User;

import java.time.LocalDateTime;

/**
 * Entity class representing a PasteBin.
 * Maps to the "paste_bins" table in the database.
 */
@Entity
@Table(name = "paste_bins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasteBin {
    /**
     * Unique identifier for the PasteBin.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who created the PasteBin.
     * Many-to-one relationship with the User entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Name of the PasteBin.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Content of the PasteBin.
     * Stored as encrypted text.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Timestamp when the PasteBin was created.
     * Automatically populated by the database.
     */
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Flag indicating whether the PasteBin is deleted.
     * Defaults to false.
     */
    @Column(nullable = false)
    private Boolean isDeleted = false;
}