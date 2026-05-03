package edu.ehei.gitdock.gitdockproject.repository;

import edu.ehei.gitdock.gitdockproject.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByCompanyId(Long companyId);

    // On utilise directement l'attribut userId défini dans UserProject
    @Query("SELECT DISTINCT p FROM Project p JOIN p.userProjects up WHERE up.userId = :userId")
    List<Project> findByManagedById(Long managedById);

    List<Project> findByUrlContainingIgnoreCase(String url); // ✅ AJOUT IMPORTANT

    @Query("""
        SELECT p
        FROM Project p
        WHERE p.id IN (
            SELECT up.project.id
            FROM UserProject up
            WHERE up.userId = :userId
        )
    """)
    List<Project> findAssignedProjects(@Param("userId") Long userId);

    // Remplacement de findByManagedBy(UserAccount user)
    List<Project> findByManagedById(Long managedById);

    List<Project> findByUrlContainingIgnoreCase(String urlPart);
}