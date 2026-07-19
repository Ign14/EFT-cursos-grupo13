package com.grupo13.bff.messaging;

import java.io.Serializable;

/**
 * Mensaje de trabajo asincrono del dominio de cursos. Ej: entrega de examen o
 * solicitud de inscripcion a procesar en segundo plano.
 *
 * forzarError=true permite demostrar el manejo de fallos (dead-letter a la DLQ).
 */
public record MensajeEft(
        String tipo,          // ENTREGA_EXAMEN | INSCRIPCION | ...
        Long cursoId,
        String estudiante,
        String detalle,
        boolean forzarError
) implements Serializable {}
