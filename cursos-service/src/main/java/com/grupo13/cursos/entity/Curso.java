package com.grupo13.cursos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "curso")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Curso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    /** Instructor (email/nombre extraido del token B2C que lo creo). */
    @Column(nullable = false)
    private String instructor;

    private Integer cupos;

    /** Clave del material del curso en S3 (null si aun no se ha subido). */
    private String materialS3Key;

    @Column(nullable = false)
    private Instant creadoEn;
}
