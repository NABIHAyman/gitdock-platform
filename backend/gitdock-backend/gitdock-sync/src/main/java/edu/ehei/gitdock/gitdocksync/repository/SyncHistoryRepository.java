package edu.ehei.gitdock.gitdocksync.repository;

import edu.ehei.gitdock.gitdocksync.model.SyncHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyncHistoryRepository extends JpaRepository<SyncHistory, Long> {
}