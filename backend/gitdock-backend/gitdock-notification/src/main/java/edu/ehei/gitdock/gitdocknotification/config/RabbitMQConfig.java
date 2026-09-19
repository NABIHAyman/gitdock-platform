package edu.ehei.gitdock.gitdocknotification.config;

import edu.ehei.gitdock.gitdocknotification.dto.NotificationEventDTO;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.support.converter.DefaultClassMapper; // 👈 Import
import java.util.HashMap; // 👈 Import
import java.util.Map; // 👈 Import

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "gitdock.exchange";
    public static final String QUEUE_NOTIFICATION = "notification.queue";
    public static final String ROUTING_KEY_NOTIFICATION = "notification.routing.key";

    // 1. Déclaration de la file d'attente (La boîte aux lettres)
    @Bean
    public Queue notificationQueue() {
        return new Queue(QUEUE_NOTIFICATION, true); // true = durable (survit aux redémarrages)
    }

    // 2. Déclaration de l'Exchange (Le centre de tri)
    @Bean
    public TopicExchange gitdockExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // 3. Le Binding : On dit au centre de tri d'envoyer les messages avec cette Routing Key dans cette Queue
    @Bean
    public Binding bindingNotification(Queue notificationQueue, TopicExchange gitdockExchange) {
        return BindingBuilder.bind(notificationQueue).to(gitdockExchange).with(ROUTING_KEY_NOTIFICATION);
    }

    // 4. Le traducteur : Transforme le JSON reçu en objet Java
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        // 👇 LE TRADUCTEUR UNIVERSEL
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("*"); // Fait confiance à tout le monde

        // Mappe les DTOs des autres services vers NOTRE DTO local
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("edu.ehei.gitdock.gitdockauth.dto.NotificationEventDTO", NotificationEventDTO.class);
        idClassMapping.put("edu.ehei.gitdock.gitdockproject.dto.NotificationEventDTO", NotificationEventDTO.class);
        idClassMapping.put("edu.ehei.gitdock.gitdocksync.dto.NotificationEventDTO", NotificationEventDTO.class);

        classMapper.setIdClassMapping(idClassMapping);
        converter.setClassMapper(classMapper);

        return converter;
    }
}