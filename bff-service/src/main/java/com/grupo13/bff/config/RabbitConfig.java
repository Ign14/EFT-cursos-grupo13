package com.grupo13.bff.config;

import com.grupo13.bff.messaging.MensajeEft;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Colas RabbitMQ del BFF (EFT S9).
 *
 * - COLA PRINCIPAL (eft.cola.principal): trabajo asincrono. Tiene un Dead Letter
 *   Exchange (DLX) configurado: si el consumidor rechaza un mensaje, RabbitMQ lo
 *   enruta automaticamente a la COLA DE ERRORES (DLQ).
 * - COLA DE ERRORES / DLQ (eft.cola.errores): bound al DLX.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE       = "eft.exchange";
    public static final String COLA_PRINCIPAL = "eft.cola.principal";
    public static final String RK_PRINCIPAL   = "eft.rk.principal";

    public static final String DLX            = "eft.dlx";
    public static final String COLA_ERRORES   = "eft.cola.errores";
    public static final String RK_ERROR       = "eft.rk.error";

    @Bean
    public DirectExchange exchange() { return new DirectExchange(EXCHANGE, true, false); }

    @Bean
    public DirectExchange deadLetterExchange() { return new DirectExchange(DLX, true, false); }

    @Bean
    public Queue colaPrincipal() {
        return QueueBuilder.durable(COLA_PRINCIPAL)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", RK_ERROR)
                .build();
    }

    @Bean
    public Queue colaErrores() { return QueueBuilder.durable(COLA_ERRORES).build(); }

    @Bean
    public Binding bindingPrincipal(Queue colaPrincipal, DirectExchange exchange) {
        return BindingBuilder.bind(colaPrincipal).to(exchange).with(RK_PRINCIPAL);
    }

    @Bean
    public Binding bindingErrores(Queue colaErrores, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(colaErrores).to(deadLetterExchange).with(RK_ERROR);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setDefaultType(MensajeEft.class);
        classMapper.setTrustedPackages("com.grupo13.bff.messaging");
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter jsonMessageConverter) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(jsonMessageConverter);
        return t;
    }
}
