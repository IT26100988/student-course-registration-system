package com.example.studentregistration.accounts.model;

import java.time.Instant;

/**
 * Abstract parent for Student and Admin.
 * Common fields (identity + audit) live here. Subclasses add role-specific data.
 */
public abstract class User {

    private String id;              // e.g. STU_... or ADM_...
    private String fullName;
    private String email;
    private String passwordHash;    // BCrypt hash, NEVER plaintext
    private AccountStatus status;   // ACTIVE / INACTIVE
    private Instant createdAt;
    private Instant updatedAt;

    protected User(String id,
                   String fullName,
                   String email,
                   String passwordHash,
                   AccountStatus status,
                   Instant createdAt,
                   Instant updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Every subclass MUST say what role it is. */
    public abstract UserRole getRole();

    // ---- encapsulated getters/setters ----
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
