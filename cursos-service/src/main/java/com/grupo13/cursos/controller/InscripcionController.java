package com.grupo13.cursos.controller;

import com.grupo13.cursos.dto.InscripcionRequest;
import com.grupo13.cursos.entity.Inscripcion;
import com.grupo13.cursos.security.UsuarioActual;
import com.grupo13.cursos.service.CursoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Inscripciones de estudiantes a cursos. */
@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final CursoService service;

    public InscripcionController(CursoService service) { this.service = service; }

    /** El estudiante autenticado se inscribe en un curso. */
    @PostMapping
    public ResponseEntity<Inscripcion> inscribir(@Valid @RequestBody InscripcionRequest req) {
        Inscripcion i = service.inscribir(req.cursoId(), UsuarioActual.email());
        return ResponseEntity.ok(i);
    }

    /** Lista las inscripciones del estudiante autenticado. */
    @GetMapping("/mias")
    public List<Inscripcion> mias() {
        return service.inscripcionesDe(UsuarioActual.email());
    }
}
