package edu.ehei.gitdock.gitdockproject.service.interfaces;

import edu.ehei.gitdock.gitdockproject.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interface définissant les services de gestion de projet.
 */
public interface IProjectService {

    /**
     * Crée un nouveau projet et assigne le créateur comme manager.
     */
    ProjectDTO createProject(CreateProjectRequestDTO requestDto);

    /**
     * Récupère tous les projets auxquels l'utilisateur courant a accès.
     */
    List<ProjectDTO> getAllProjects();

    /**
     * Récupère la liste des collaborateurs groupés par projet.
     */
    List<CollaboratorGroupedDTO> getCollaboratorsGrouped();

    /**
     * Retire un collaborateur d'un projet spécifique.
     */
    void removeCollaborator(Long projectId, Long collaboratorId);

    /**
     * Récupère les détails d'un projet par son identifiant.
     */
    ProjectDTO getProjectById(Long id);

    /**
     * Liste toutes les branches enregistrées pour un projet.
     */
    List<BranchDTO> getProjectBranches(Long projectId);

    /**
     * Liste les commits d'un projet (optionnellement filtrés par branche).
     */
    Page<CommitDTO> getProjectCommits(Long projectId, Long branchId, int page, int size);

    List<CollaboratorGroupedDTO.CollaboratorDTO> getProjectCollaborators(Long projectId);

    void addCollaborator(Long projectId, AddCollaboratorDTO request);

    void deleteProject(Long id);

    void assignToGhostUser(Long deletedUserId);

    String getCommitDiff(Long projectId, String hash);

}