package edu.ehei.gitdock.gitdockauth.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "gitdock.exchange";

    // 1. Déclaration de l'Exchange (Le centre de tri)
    @Bean
    public TopicExchange gitdockExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // 2. Le traducteur : Transforme nos objets Java (DTO) en JSON pour RabbitMQ
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}