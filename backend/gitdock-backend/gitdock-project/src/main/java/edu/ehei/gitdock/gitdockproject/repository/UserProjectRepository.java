package edu.ehei.gitdock.gitdockproject.repository;

import edu.ehei.gitdock.gitdockproject.enums.ProjectRole;
import edu.ehei.gitdock.gitdockproject.model.Project;
import edu.ehei.gitdock.gitdockproject.model.UserProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProject, Long> {

    boolean existsByUserIdAndProject(Long userId, Project project);

    @Modifying
    @Query("DELETE FROM UserProject up WHERE up.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserProject up WHERE up.project.id = :projectId AND up.userId = :userId")
    void removeUserFromProject(@Param("projectId") Long projectId,
                               @Param("userId") Long userId);

    // On utilise roleId au lieu de RoleName, car le nom du rôle est dans gitdock-auth !
    boolean existsByProjectIdAndUserIdAndRole(Long projectId, Long userId, ProjectRole role);


}