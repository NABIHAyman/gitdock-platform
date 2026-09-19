package edu.ehei.gitdock.gitdockproject.service;

import edu.ehei.gitdock.gitdockproject.config.CacheConfig;
import edu.ehei.gitdock.gitdockproject.config.RabbitMQConfig;
import edu.ehei.gitdock.gitdockproject.dto.CommitEnrichmentResultDTO;
import edu.ehei.gitdock.gitdockproject.dto.CommitSavedEventDTO;
import edu.ehei.gitdock.gitdockproject.messaging.RabbitMQProducer;
import edu.ehei.gitdock.gitdockproject.repository.CommitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrichmentResultListener {

    private final CommitRepository commitRepository;
    private final RabbitMQProducer rabbitMQProducer;
    private final StringRedisTemplate stringRedisTemplate; // 👈 AJOUT POUR LE CACHE

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ENRICH_RESULT)
    @Transactional
    public void handleEnrichmentResult(CommitEnrichmentResultDTO result) {
        log.info("📊 Réception des statistiques pour le commit {}", result.getCommitSha().substring(0, 7));

        commitRepository.findByHashAndProjectId(result.getCommitSha(), result.getProjectId())
                .ifPresent(commit -> {
                    // 1. Mise à jour des stats réelles
                    commit.setAdditions(result.getAdditions());
                    commit.setDeletions(result.getDeletions());
                    commitRepository.save(commit);

                    // 2. 🎮 DÉCLENCHEMENT GAMIFICATION (Maintenant on a l'XP exact !)
                    if (result.getAuthorUserId() != null) {
                        CommitSavedEventDTO event = CommitSavedEventDTO.builder()
                                .commitId(commit.getId())
                                .hash(commit.getHash())
                                .message(commit.getMessage())
                                .projectId(result.getProjectId())
                                .branchId(commit.getBranch() != null ? commit.getBranch().getId() : null)
                                .authorUserId(result.getAuthorUserId())
                                .additions(result.getAdditions())
                                .deletions(result.getDeletions())
                                .build();
                        rabbitMQProducer.sendCommitSavedEvent(event);
                    }

                    // 👇 3. INVALIDATION DU CACHE (Pour le Frontend Temps Réel)
                    try {
                        org.springframework.data.redis.core.ScanOptions opts = org.springframework.data.redis.core.ScanOptions.scanOptions()
                                .match(CacheConfig.CACHE_COMMITS + "::" + result.getProjectId() + "*")
                                .count(100).build();

                        List<String> toDelete = new ArrayList<>();
                        try (org.springframework.data.redis.core.Cursor<byte[]> cursor = stringRedisTemplate.getConnectionFactory().getConnection().scan(opts)) {
                            while (cursor.hasNext()) {
                                toDelete.add(new String(cursor.next()));
                            }
                        }
                        if (!toDelete.isEmpty()) {
                            stringRedisTemplate.delete(toDelete);
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ Cache non purgé après enrichissement pour projet {}", result.getProjectId());
                    }
                });
    }
}