package edu.ehei.gitdock.gitdockproject.config; // ⚠️ À adapter pour gitdock-sync

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "gitdock.exchange";
    public static final String QUEUE_SYNC_REQUEST = "gitdock.sync.request.queue";
    public static final String QUEUE_SYNC_RESULT = "gitdock.sync.result.queue";
    public static final String ROUTING_KEY_REQUEST = "sync.request";
    public static final String ROUTING_KEY_RESULT = "sync.result";
    public static final String ROUTING_KEY_NOTIFICATION = "notification.routing.key";
    public static final String ROUTING_KEY_WEBHOOK = "webhook.result";
    public static final String QUEUE_WEBHOOK_RESULT = "gitdock.webhook.result.queue";
    public static final String ROUTING_KEY_COMMIT_SAVED = "project.commit.saved";
    public static final String ROUTING_KEY_COLLABORATOR_ADDED = "collaborator.added.event";

    @Bean
    public TopicExchange gitdockExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // TopicExchance VS DirectExchange ?

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

    // Indispensable pour que RabbitMQ sérialise nos objets Java en JSON (et inversement)
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue webhookResultQueue() {
        return new Queue(QUEUE_WEBHOOK_RESULT, true);
    }

    @Bean
    public Binding bindingWebhook(Queue webhookResultQueue, TopicExchange gitdockExchange) {
        return BindingBuilder.bind(webhookResultQueue).to(gitdockExchange).with(ROUTING_KEY_WEBHOOK);
    }
}