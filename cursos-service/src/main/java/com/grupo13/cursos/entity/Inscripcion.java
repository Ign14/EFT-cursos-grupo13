package com.grupo13.cursos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "inscripcion",
       uniqueConstraints = @UniqueConstraint(columnNames = {"curso_id", "estudiante"}))
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Inscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "curso_id", nullable = false)
    private Long cursoId;

    @Column(nullable = false)
    private String estudiante;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private String estado;   // INSCRITO
}
