package edu.ehei.gitdock.gitdockproject.controller;

import edu.ehei.gitdock.gitdockproject.dto.ProjectResponseDTO;
import edu.ehei.gitdock.gitdockproject.dto.SagaProjectCreationRequestDTO;
import edu.ehei.gitdock.gitdockproject.service.UsesCases.ProjectSagaOrchestrator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/saga")
@RequiredArgsConstructor
public class ProjectSagaController {

    private final ProjectSagaOrchestrator sagaOrchestrator;

    @PostMapping("/create-with-team")
    public ResponseEntity<ProjectResponseDTO> createProjectWithTeam(
            @RequestBody @Valid SagaProjectCreationRequestDTO request,
            @RequestHeader("X-User-Id") Long managerId,
            HttpServletRequest httpRequest) { // Injecté par la Gateway

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null) {
            authHeader = httpRequest.getHeader("authorization"); // Fallback minuscule
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Action refusée : Token JWT manquant.");
        }

        // On lit directement le header injecté par la Gateway !
        String companyIdStr = httpRequest.getHeader("X-Company-Id");
        Long companyId = (companyIdStr != null && !companyIdStr.isBlank() && !"null".equals(companyIdStr))
                ? Long.valueOf(companyIdStr) : null;

        if (companyId == null) {
            throw new RuntimeException("Action refusée : Aucune entreprise associée à votre profil.");
        }

        // 2. On passe le companyId à l'Orchestrateur
        ProjectResponseDTO response = sagaOrchestrator.executeProjectCreationSaga(request, managerId, companyId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}