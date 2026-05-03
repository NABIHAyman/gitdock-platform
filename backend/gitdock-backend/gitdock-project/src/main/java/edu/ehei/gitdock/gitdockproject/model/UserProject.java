package edu.ehei.gitdock.gitdockproject.model;

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
@Table(name = "user_project")
public class UserProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID venant du microservice auth
    @Column(name="user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    private ProjectRole role;

    private LocalDateTime assignedAt;

    private Long assignedById;
}