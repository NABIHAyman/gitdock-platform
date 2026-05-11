package edu.ehei.gitdock.gitdockproject.model;

import edu.ehei.gitdock.gitdockproject.enums.ProjectPlatform;
import edu.ehei.gitdock.gitdockproject.enums.ProjectStatus;
import edu.ehei.gitdock.gitdockproject.enums.ProjectVisibility;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "project", uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "name"}))
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lien "logique" vers gitdock-auth (Company)
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @NotBlank
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Lien "logique" vers gitdock-auth (UserAccount)
    @Column(name = "managed_by_id")
    private Long managedById;

    @Column(name = "repo_id", unique = true)
    private String repoId;

    @NotBlank
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private ProjectPlatform platform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectVisibility visibility;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProjectStatus status; // Le statut essentiel pour la Saga

    // --- RELATIONS CONSERVÉES DANS GITDOCK-PROJECT ---

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Part> parts = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Branch> branches = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Commit> commits = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserProject> userProjects = new ArrayList<>();

    // --------------------------------------------------

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Note : J'ai retiré le @PreRemove qui faisait commit.setTask(null)
    // car la logique Task (PHP/Symfony) n'est plus ici.
    // Si un commit fait référence à une tâche, il aura simplement un champ "taskId (Long)".
}