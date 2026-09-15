package com.challenge.aguiabranca.api.user.domain;

import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
@CompoundIndex(name = "role_active_idx", def = "{'role': 1, 'active': 1}")
public class UserDocument {
    @Id private String id;
    private String name;
    @Indexed(unique = true) private String email;
    private String passwordHash;
    private Role role;
    private boolean active = true;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;

    public UserDocument() {}

    public UserDocument(String name, String email, String passwordHash, Role role) {
        this.name = name.trim();
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = true;
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setActive(boolean active) { this.active = active; }
}

