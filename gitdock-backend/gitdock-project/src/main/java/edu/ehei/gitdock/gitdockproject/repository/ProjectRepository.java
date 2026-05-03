package edu.ehei.gitdock.gitdockproject.repository;

import edu.ehei.gitdock.gitdockproject.model.Project;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByCompanyId(Long companyId);

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
}