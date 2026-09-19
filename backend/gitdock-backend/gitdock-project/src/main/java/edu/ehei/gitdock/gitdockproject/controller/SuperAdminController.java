package edu.ehei.gitdock.gitdockproject.controller;

import edu.ehei.gitdock.gitdockproject.repository.CommitRepository;
import edu.ehei.gitdock.gitdockproject.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/projects/super-admin")
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')") // 🛡️
@RequiredArgsConstructor
public class SuperAdminController {

    private final ProjectRepository projectRepository;
    private final CommitRepository commitRepository;

    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Long>> getPlatformKPIs() {
        // Opérations ultra-rapides (COUNT SQL)
        long totalProjects = projectRepository.count();
        long totalCommits = commitRepository.count();

        return ResponseEntity.ok(Map.of(
                "totalProjects", totalProjects,
                "totalCommits", totalCommits
        ));
    }
}