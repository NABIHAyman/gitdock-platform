package edu.ehei.gitdock.gitdocksync.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "raw-commits";

    public void sendCommitToSentinel(String hash, String projectId, String diff, String author) {
        try {
            Map<String, String> payload = Map.of(
                    "hash", hash,
                    "project_id", projectId,
                    "diff", diff,
                    "author", author,
                    "source", "gitdock-sync" // On indique que ça vient de l'aspirateur officiel
            );
            String message = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(TOPIC, hash, message);
            log.info("🚀 Code source du commit {} poussé dans [raw-commits] pour nettoyage Spark.", hash.substring(0, 7));
        } catch (Exception e) {
            log.error("❌ Échec d'envoi vers Kafka : {}", e.getMessage());
        }
    }
}