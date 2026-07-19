package com.grupo13.bff.messaging;

import com.grupo13.bff.config.RabbitConfig;
import com.rabbitmq.client.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CONSUMIDOR: procesa la COLA PRINCIPAL bajo demanda (endpoint /consumir).
 *
 * Usa ack manual: por cada mensaje, si procesa OK hace basicAck; si falla hace
 * basicNack(requeue=false), con lo que RabbitMQ lo DEAD-LETTEREA a la cola de
 * errores (DLQ) a traves del DLX configurado en la cola principal. Asi el flujo
 * cola principal -> DLX -> DLQ ocurre por el mecanismo real de RabbitMQ.
 */
@Slf4j
@Service
public class ColaConsumerService {

    private final RabbitTemplate rabbitTemplate;
    private final MessageConverter converter;
    private final MessagePropertiesConverter propsConverter = new DefaultMessagePropertiesConverter();

    public ColaConsumerService(RabbitTemplate rabbitTemplate, MessageConverter jsonMessageConverter) {
        this.rabbitTemplate = rabbitTemplate;
        this.converter = jsonMessageConverter;
    }

    /** Drena la cola principal procesando cada mensaje; devuelve el resumen. */
    public Map<String, Object> consumir() {
        return rabbitTemplate.execute(channel -> {
            int procesados = 0;
            int errores = 0;
            while (true) {
                GetResponse r = channel.basicGet(RabbitConfig.COLA_PRINCIPAL, false); // autoAck=false
                if (r == null) break;
                long tag = r.getEnvelope().getDeliveryTag();
                try {
                    MessageProperties props = propsConverter.toMessageProperties(
                            r.getProps(), r.getEnvelope(), "UTF-8");
                    MensajeEft msg = (MensajeEft) converter.fromMessage(new Message(r.getBody(), props));
                    if (msg.forzarError()) {
                        throw new IllegalStateException("Mensaje marcado para forzar error");
                    }
                    procesar(msg);
                    channel.basicAck(tag, false);
                    procesados++;
                } catch (Exception ex) {
                    log.warn("[CONSUMIDOR] FALLO -> nack (no requeue) -> DLQ via DLX: {}", ex.getMessage());
                    channel.basicNack(tag, false, false); // requeue=false => dead-letter
                    errores++;
                }
            }
            Map<String, Object> resumen = new LinkedHashMap<>();
            resumen.put("procesados", procesados);
            resumen.put("errores", errores);
            log.info("[CONSUMIDOR] Resumen: {}", resumen);
            return resumen;
        });
    }

    /** Logica de negocio del procesamiento del mensaje (demostrativa). */
    private void procesar(MensajeEft msg) {
        log.info("[CONSUMIDOR] OK -> procesando tipo={} curso={} estudiante={} detalle={}",
                msg.tipo(), msg.cursoId(), msg.estudiante(), msg.detalle());
    }

    /** Lista (drena) los mensajes dead-lettered de la DLQ (evidencia de fallos). */
    public List<MensajeEft> verErrores() {
        List<MensajeEft> lista = new ArrayList<>();
        Object obj;
        while ((obj = rabbitTemplate.receiveAndConvert(RabbitConfig.COLA_ERRORES)) != null) {
            lista.add((MensajeEft) obj);
        }
        return lista;
    }
}
