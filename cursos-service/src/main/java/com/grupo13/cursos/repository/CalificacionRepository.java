package com.grupo13.cursos.repository;

import com.grupo13.cursos.entity.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    List<Calificacion> findByEstudiante(String estudiante);
    List<Calificacion> findByCursoId(Long cursoId);
}
