package edu.ehei.gitdock.gitdockproject.controller;

import edu.ehei.gitdock.gitdockproject.dto.*;
import edu.ehei.gitdock.gitdockproject.service.interfaces.IProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final IProjectService projectService;

    // GET http://localhost:8080/api/projects
    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    // POST http://localhost:8080/api/projects
    // POST http://localhost:8080/api/projects
    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody CreateProjectRequestDTO request) {
        try {
            return ResponseEntity.ok(projectService.createProject(request));
        } catch (RuntimeException e) {
            // 👇 On intercepte notre mot magique pour le Frontend 👇
            if ("OAUTH_REQUIRED".equals(e.getMessage())) {
                // On utilise 428 (Precondition Required) ou 400 pour NE PAS déclencher
                // la déconnexion automatique de l'intercepteur Axios !
                return ResponseEntity.status(428).body(Map.of("message", "OAUTH_REQUIRED"));
            }
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // GET http://localhost:8080/api/projects/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_COMPANY_ADMIN', 'ROLE_WORKSPACE_OWNER', 'ROLE_MANAGER', 'ROLE_DEVELOPER')")
    public ResponseEntity<ProjectDTO> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    // GET http://localhost:8080/api/projects/dashboard/collaborators-grouped
    @GetMapping("/dashboard/collaborators-grouped")
    public ResponseEntity<List<CollaboratorGroupedDTO>> getCollaboratorsGrouped() {
        return ResponseEntity.ok(projectService.getCollaboratorsGrouped());
    }

    // DELETE http://localhost:8080/api/projects/{projectId}/collaborators/{userId}
    @DeleteMapping("/{projectId}/collaborators/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_COMPANY_ADMIN', 'ROLE_WORKSPACE_OWNER', 'ROLE_MANAGER', 'ROLE_DEVELOPER')")
    public ResponseEntity<Void> removeCollaborator(@PathVariable Long projectId, @PathVariable Long userId) {
        projectService.removeCollaborator(projectId, userId);
        return ResponseEntity.ok().build();
    }

    // GET http://localhost:8080/api/projects/{projectId}/branches
    @GetMapping("/{projectId}/branches")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_COMPANY_ADMIN', 'ROLE_WORKSPACE_OWNER', 'ROLE_MANAGER', 'ROLE_DEVELOPER')")
    public ResponseEntity<List<BranchDTO>> getProjectBranches(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectBranches(projectId));
    }

    // GET http://localhost:8080/api/projects/{projectId}/branches/{branchId}/commits
    @GetMapping("/{projectId}/branches/{branchId}/commits")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_COMPANY_ADMIN', 'ROLE_WORKSPACE_OWNER', 'ROLE_MANAGER', 'ROLE_DEVELOPER')")
    public ResponseEntity<Page<CommitDTO>> getBranchCommits(@PathVariable Long projectId,
                                                            @PathVariable Long branchId,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(projectService.getProjectCommits(projectId, branchId, page, size));
    }

    // GET /api/projects/{projectId}/collaborators
    @GetMapping("/{projectId}/collaborators")
    public ResponseEntity<List<CollaboratorGroupedDTO.CollaboratorDTO>> getProjectCollaborators(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectCollaborators(projectId));
    }

    // POST /api/projects/{projectId}/collaborators
    @PostMapping("/{projectId}/collaborators")
    public ResponseEntity<Void> addCollaborator(@PathVariable Long projectId, @RequestBody AddCollaboratorDTO request) {
        projectService.addCollaborator(projectId, request);
        return ResponseEntity.ok().build();
    }

    // DELETE http://localhost:8080/api/projects/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_COMPANY_ADMIN', 'ROLE_WORKSPACE_OWNER', 'ROLE_MANAGER')")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateProject(DataIntegrityViolationException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "message", "Un projet avec ce nom existe déjà dans votre espace de travail."
        ));
    }

}