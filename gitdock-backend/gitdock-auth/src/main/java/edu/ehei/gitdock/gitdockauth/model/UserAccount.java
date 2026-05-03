package edu.ehei.gitdock.gitdockauth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_account")
public class UserAccount implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    @JsonIgnore
    private Company company;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    private String firstName;
    private String lastName;

    private String avatarUrl;

    private String githubId;
    private String gitlabId;
    private String bitbucketId;

    @Builder.Default
    @Column(name = "account_locked")
    private boolean accountLocked = false;

    @Builder.Default
    @Column(name = "is_enabled")
    private boolean isEnabled = false;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore
    @Builder.Default
    private List<UserRole> userRoles = new ArrayList<>();

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (this.userRoles == null) {
            return authorities;
        }

        for (UserRole userRole : userRoles) {
            if (userRole.getRole() != null) {
                String roleName = userRole.getRole().getName().name();
                authorities.add(new SimpleGrantedAuthority(roleName));
            }
        }
        return authorities;
    }

    public void addRole(Role role, Company company) {
        boolean alreadyExists = this.userRoles.stream().anyMatch(ur ->
                ur.getRole().equals(role) &&
                        Objects.equals(ur.getCompany(), company));

        if (!alreadyExists) {
            UserRole userRole = UserRole.builder()
                    .user(this)
                    .role(role)
                    .company(company)
                    .build();

            this.userRoles.add(userRole);
        }
    }

    public void removeRoles() {
        this.userRoles.clear();
    }

    public String getPassword() { return password; }
    public String getUsername() { return email; }
    public boolean isAccountNonExpired() { return true; }
    public boolean isAccountNonLocked() { return !accountLocked; }
    public boolean isCredentialsNonExpired() { return true; }
    public boolean isEnabled() { return isEnabled; }

    public String getFullName() { return firstName + " " + lastName; }
    public void setPasswordHash(String encoded) { this.password = encoded; }
}