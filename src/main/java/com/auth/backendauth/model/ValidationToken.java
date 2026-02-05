package com.auth.backendauth.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ValidationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String tokenValue;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    // ======= GETTERS & SETTERS =======
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTokenValue() { return tokenValue; }
    public void setTokenValue(String tokenValue) { this.tokenValue = tokenValue; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isUsed() { return used; }   // 👈 attention : isUsed() pour boolean
    public void setUsed(boolean used) { this.used = used; }

    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }
}
