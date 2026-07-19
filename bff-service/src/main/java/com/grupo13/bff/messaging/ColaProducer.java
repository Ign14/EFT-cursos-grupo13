package com.grupo13.bff.messaging;

import com.grupo13.bff.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * PRODUCTOR: publica mensajes de trabajo en la COLA PRINCIPAL a traves del exchange.
 */
@Slf4j
@Service
public class ColaProducer {

    private final RabbitTemplate rabbitTemplate;

    public ColaProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void producir(MensajeEft mensaje) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.RK_PRINCIPAL, mensaje);
        log.info("[PRODUCTOR] Mensaje publicado en {}: tipo={} curso={} estudiante={}",
                RabbitConfig.COLA_PRINCIPAL, mensaje.tipo(), mensaje.cursoId(), mensaje.estudiante());
    }
}
