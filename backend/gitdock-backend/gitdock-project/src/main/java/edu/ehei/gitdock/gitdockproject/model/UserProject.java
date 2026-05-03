package edu.ehei.gitdock.gitdockproject.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.ehei.gitdock.gitdockproject.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_project", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "project_id"})
})
public class UserProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID venant du microservice auth
    @Column(name="user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false)
    private ProjectRole role;

    private LocalDateTime assignedAt;

    // Lien logique vers gitdock-auth (UserAccount)
    @Column(name = "assigned_by_id")
    private Long assignedById;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    protected void onCreate() {
        this.assignedAt = LocalDateTime.now();
    }
}