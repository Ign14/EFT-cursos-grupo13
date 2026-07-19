package com.grupo13.cursos.repository;

import com.grupo13.cursos.entity.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    List<Inscripcion> findByEstudiante(String estudiante);
    List<Inscripcion> findByCursoId(Long cursoId);
    boolean existsByCursoIdAndEstudiante(Long cursoId, String estudiante);
}
