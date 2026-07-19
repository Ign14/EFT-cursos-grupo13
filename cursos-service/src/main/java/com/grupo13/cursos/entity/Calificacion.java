package com.grupo13.cursos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "calificacion")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Calificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "curso_id", nullable = false)
    private Long cursoId;

    @Column(nullable = false)
    private String estudiante;

    @Column(nullable = false)
    private String examen;

    @Column(nullable = false)
    private Double nota;

    @Column(nullable = false)
    private LocalDate fecha;
}
