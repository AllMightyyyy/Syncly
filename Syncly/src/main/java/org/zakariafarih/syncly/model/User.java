package org.zakariafarih.syncly.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entity class representing a User.
 * Maps to the "users" table in the database.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    /**
     * Unique identifier for the User.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Username of the User.
     * Must be unique and not null.
     */
    @Column(unique = true, nullable = false)
    private String username;

    // TODO -> add displayName and avatarUrl to the database
    private String displayName;

    private String avatarUrl;

    /**
     * Email of the User.
     * Must be unique and not null.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Hashed password of the User.
     * Must not be null.
     */
    @Column(nullable = false)
    private String passwordHash;

    /**
     * Role of the User.
     * Defaults to USER.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    /**
     * Timestamp when the User was created.
     * Automatically populated by the database.
     */
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Timestamp when the User was last updated.
     * Automatically populated by the database.
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean IsEmailVerified = false;

    private String emailVerificationToken;

    /**
     * Enum representing the role of the User.
     */
    public enum Role {
        /**
         * Represents an admin user.
         */
        ADMIN,

        /**
         * Represents a regular user.
         */
        USER
    }

    private String resetPasswordToken;

    private LocalDateTime resetPasswordExpiresAt;

    @Column(nullable = false)
    private boolean IsActive = false;

    private String twoFactorSecret;

    @Column(nullable = false)
    private boolean IsTwoFactorEnabled = false;

    // OAuth2 specific fields
    private String provider; // e.g., google
    private String providerId; // Unique ID from the OAuth2 provider

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Device> devices;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PasswordHistory> passwordHistories;
}