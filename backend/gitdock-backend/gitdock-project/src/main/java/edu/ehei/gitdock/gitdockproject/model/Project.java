package edu.ehei.gitdock.gitdockproject.model;

import edu.ehei.gitdock.gitdockproject.enums.ProjectPlatform;
import edu.ehei.gitdock.gitdockproject.enums.ProjectStatus;
import edu.ehei.gitdock.gitdockproject.enums.ProjectVisibility;
import jakarta.persistence.*;
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
@Table(name = "project",
        uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "name"}))
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="company_id", nullable = false)
    private Long companyId;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name="managed_by_id")
    private Long managedById;

    private String url;

    @Enumerated(EnumType.STRING)
    private ProjectPlatform platform;

    @Enumerated(EnumType.STRING)
    private ProjectVisibility visibility;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 🔥 IMPORTANT RELATION CORRECTE
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserProject> userProjects = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}