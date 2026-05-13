package edu.ehei.gitdock.gitdocksync.config; // ⚠️ À adapter pour gitdock-sync

import edu.ehei.gitdock.gitdocksync.dto.SyncRequestMessageDTO;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "gitdock.exchange";
    public static final String QUEUE_SYNC_REQUEST = "gitdock.sync.request.queue";
    public static final String QUEUE_SYNC_RESULT = "gitdock.sync.result.queue";
    public static final String ROUTING_KEY_REQUEST = "sync.request";
    public static final String ROUTING_KEY_RESULT = "sync.result";
    public static final String ROUTING_KEY_NOTIFICATION = "notification.routing.key";
    public static final String ROUTING_KEY_WEBHOOK = "webhook.result";

    public static final String QUEUE_ENRICH_REQUEST = "gitdock.enrich.request.queue";
    public static final String QUEUE_ENRICH_RESULT = "gitdock.enrich.result.queue";
    public static final String ROUTING_KEY_ENRICH_REQUEST = "enrich.request";
    public static final String ROUTING_KEY_ENRICH_RESULT = "enrich.result";

    @Bean
    public TopicExchange gitdockExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue syncRequestQueue() {
        return new Queue(QUEUE_SYNC_REQUEST, true); // true = durable
    }

    @Bean
    public Queue syncResultQueue() {
        return new Queue(QUEUE_SYNC_RESULT, true);
    }

    @Bean
    public Binding bindingRequest(Queue syncRequestQueue, TopicExchange gitdockExchange) {
        return BindingBuilder.bind(syncRequestQueue).to(gitdockExchange).with(ROUTING_KEY_REQUEST);
    }

    @Bean
    public Binding bindingResult(Queue syncResultQueue, TopicExchange gitdockExchange) {
        return BindingBuilder.bind(syncResultQueue).to(gitdockExchange).with(ROUTING_KEY_RESULT);
    }

    @Bean public Queue enrichRequestQueue() { return new Queue(QUEUE_ENRICH_REQUEST, true); }
    @Bean public Queue enrichResultQueue() { return new Queue(QUEUE_ENRICH_RESULT, true); }

    @Bean public Binding bindingEnrichReq(Queue enrichRequestQueue, TopicExchange gitdockExchange) {
        return BindingBuilder.bind(enrichRequestQueue).to(gitdockExchange).with(ROUTING_KEY_ENRICH_REQUEST);
    }
    @Bean public Binding bindingEnrichRes(Queue enrichResultQueue, TopicExchange gitdockExchange) {
        return BindingBuilder.bind(enrichResultQueue).to(gitdockExchange).with(ROUTING_KEY_ENRICH_RESULT);
    }

    // Indispensable pour que RabbitMQ sérialise nos objets Java en JSON (et inversement)
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("*");

        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put(
                "edu.ehei.gitdock.gitdockproject.dto.SyncRequestMessageDTO",
                SyncRequestMessageDTO.class
        );
        idClassMapping.put(
                "edu.ehei.gitdock.gitdocksync.dto.CommitEnrichmentRequestDTO",
                edu.ehei.gitdock.gitdocksync.dto.CommitEnrichmentRequestDTO.class // Adapte le package selon le service !
        );
        idClassMapping.put(
                "edu.ehei.gitdock.gitdockproject.dto.CommitEnrichmentRequestDTO",
                edu.ehei.gitdock.gitdocksync.dto.CommitEnrichmentRequestDTO.class
        );
        idClassMapping.put(
                "edu.ehei.gitdock.gitdocksync.dto.CommitEnrichmentResultDTO",
                edu.ehei.gitdock.gitdocksync.dto.CommitEnrichmentResultDTO.class
        );
        idClassMapping.put(
                "edu.ehei.gitdock.gitdockproject.dto.CommitEnrichmentResultDTO",
                edu.ehei.gitdock.gitdocksync.dto.CommitEnrichmentResultDTO.class
        );

        classMapper.setIdClassMapping(idClassMapping);
        converter.setClassMapper(classMapper);

        return converter;
    }
}