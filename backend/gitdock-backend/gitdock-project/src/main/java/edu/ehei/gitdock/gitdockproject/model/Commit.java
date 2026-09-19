package edu.ehei.gitdock.gitdockproject.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "commits", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "hash"})
})
public class Commit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lien logique vers gitdock-auth (UserAccount)
    @Column(name = "user_id")
    private Long userId;

    String authorName;
    String authorEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @NotNull
    @JsonIgnore
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    @JsonIgnore
    private Branch branch;

    // Lien logique vers gitdock-tasks (Task)
    @Column(name = "task_id")
    private Long taskId;

    @Column(length = 40, nullable = false)
    private String hash;

    @Column(name = "committed_at", nullable = false)
    private LocalDateTime committedAt;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "additions")
    @Builder.Default
    private Integer additions = 0;

    @Column(name = "deletions")
    @Builder.Default
    private Integer deletions = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // La relation CommitTag est gérée dans gitdock-gamification

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.committedAt == null) {
            this.committedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}