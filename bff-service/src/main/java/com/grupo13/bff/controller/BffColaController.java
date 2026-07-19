package com.grupo13.bff.controller;

import com.grupo13.bff.messaging.ColaConsumerService;
import com.grupo13.bff.messaging.ColaProducer;
import com.grupo13.bff.messaging.MensajeEft;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BFF: orquesta las llamadas a las colas RabbitMQ.
 *  - POST /api/bff/cola/producir  -> PRODUCTOR (publica en la cola principal)
 *  - POST /api/bff/cola/consumir  -> CONSUMIDOR (procesa; los que fallan van a la DLQ)
 *  - GET  /api/bff/cola/errores   -> muestra la DLQ (mensajes dead-lettered)
 */
@RestController
@RequestMapping("/api/bff/cola")
public class BffColaController {

    private final ColaProducer producer;
    private final ColaConsumerService consumer;

    public BffColaController(ColaProducer producer, ColaConsumerService consumer) {
        this.producer = producer;
        this.consumer = consumer;
    }

    public record ProducirRequest(
            @NotBlank String tipo,
            Long cursoId,
            String estudiante,
            String detalle,
            boolean error) {}

    @PostMapping("/producir")
    public ResponseEntity<MensajeEft> producir(@Valid @RequestBody ProducirRequest req) {
        MensajeEft msg = new MensajeEft(req.tipo(), req.cursoId(), req.estudiante(),
                req.detalle(), req.error());
        producer.producir(msg);
        return ResponseEntity.ok(msg);
    }

    @PostMapping("/consumir")
    public ResponseEntity<Map<String, Object>> consumir() {
        return ResponseEntity.ok(consumer.consumir());
    }

    @GetMapping("/errores")
    public ResponseEntity<List<MensajeEft>> errores() {
        return ResponseEntity.ok(consumer.verErrores());
    }
}
