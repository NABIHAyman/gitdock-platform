package edu.ehei.gitdock.gitdocksync.listener;

import edu.ehei.gitdock.gitdocksync.GitdockSyncApplication;
import edu.ehei.gitdock.gitdocksync.config.RabbitMQConfig;
import edu.ehei.gitdock.gitdocksync.dto.SyncRequestMessageDTO;
import edu.ehei.gitdock.gitdocksync.dto.SyncResultMessageDTO;
import edu.ehei.gitdock.gitdocksync.dto.SyncResultDTO;
import edu.ehei.gitdock.gitdocksync.service.ProjectSyncListener;
import edu.ehei.gitdock.gitdocksync.strategy.GitPlatformSyncFactory;
import edu.ehei.gitdock.gitdocksync.strategy.IGitPlatformSyncStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = GitdockSyncApplication.class)
@TestPropertySource(properties = {
        "spring.rabbitmq.host=127.0.0.1",
        "spring.rabbitmq.port=5672",

        // On change l'utilisateur pour le super-utilisateur par défaut
        "spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/gitdock_sync",
        "spring.datasource.username=postgres",
        "spring.datasource.password=postgres", // <--- C'est le mdp par défaut de l'image

        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.jpa.show-sql=true",
        "feign.circuitbreaker.enabled=true"
})
class ProjectSyncListenerIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @SpyBean
    private ProjectSyncListener projectSyncListener;

    @MockBean
    private GitPlatformSyncFactory syncFactory;

    @MockBean
    private IGitPlatformSyncStrategy syncStrategy;

    // NOTE : On ne mocke pas SyncHistoryRepository ici
    // pour forcer l'écriture réelle dans PostgreSQL !

    @Test
    void should_ProcessSync_And_CreateDatabaseRecords() throws Exception {
        // 1. ARRANGE
        SyncRequestMessageDTO request = SyncRequestMessageDTO.builder()
                .projectId(1L)
                .projectName("Test Docker Project")
                .repoUrl("https://github.com/test/repo")
                .platform("GITHUB")
                .userId(123L)
                .build();

        SyncResultDTO mockResult = SyncResultDTO.builder()
                .success(true)
                .branches(java.util.Collections.emptyList())
                .commits(java.util.Collections.emptyList())
                .durationSeconds(5L)
                .build();

        when(syncFactory.getStrategy("GITHUB")).thenReturn(syncStrategy);
        when(syncStrategy.fetchProjectData(anyString(), anyLong())).thenReturn(mockResult);

        // 2. ACT
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_REQUEST,
                request
        );

        // 3. ASSERT
        // On attend que le listener traite le message
        verify(projectSyncListener, timeout(5000)).handleSyncRequest(any(SyncRequestMessageDTO.class));

        // On vérifie que la file de résultat a bien reçu la réponse
        SyncResultMessageDTO response = null;
        int attempts = 0;
        while (response == null && attempts < 10) {
            response = (SyncResultMessageDTO) rabbitTemplate.receiveAndConvert(RabbitMQConfig.QUEUE_SYNC_RESULT);
            if (response == null) Thread.sleep(500);
            attempts++;
        }

        assertNotNull(response, "La table devrait être créée et le message traité !");
        System.out.println("🚀 TEST RÉUSSI : Vérifie maintenant PostgreSQL !");
    }
}