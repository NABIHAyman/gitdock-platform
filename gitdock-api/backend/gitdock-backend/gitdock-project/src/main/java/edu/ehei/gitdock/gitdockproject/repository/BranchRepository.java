package edu.ehei.gitdock.gitdockproject.repository;

import edu.ehei.gitdock.gitdockproject.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    boolean existsByProjectIdAndName(Long projectId, String name);
    List<Branch> findByProjectId(Long projectId);
    Optional<Branch> findByProjectIdAndName(Long projectId, String name);
}