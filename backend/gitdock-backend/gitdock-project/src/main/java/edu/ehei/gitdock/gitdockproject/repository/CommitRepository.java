package edu.ehei.gitdock.gitdockproject.repository;

import edu.ehei.gitdock.gitdockproject.model.Commit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommitRepository extends JpaRepository<Commit, Long> {

    @Modifying
    @Query("UPDATE Commit c SET c.userId = :ghostId WHERE c.userId = :userId")
    void reassignCommitsToGhost(@Param("userId") Long userId, @Param("ghostId") Long ghostId);

    boolean existsByHashAndProjectId(String hash, Long projectId);

    Page<Commit> findByProjectIdOrderByCommittedAtDesc(Long projectId, Pageable pageable);

    Page<Commit> findByBranchIdOrderByCommittedAtDesc(Long branchId, Pageable pageable);
}