package com.grupo13.cursos.controller;

import com.grupo13.cursos.dto.CalificacionRequest;
import com.grupo13.cursos.entity.Calificacion;
import com.grupo13.cursos.security.UsuarioActual;
import com.grupo13.cursos.service.CursoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Calificaciones registradas por instructores. */
@RestController
@RequestMapping("/api/calificaciones")
public class CalificacionController {

    private final CursoService service;

    public CalificacionController(CursoService service) { this.service = service; }

    /** El instructor registra una calificacion de un examen. */
    @PostMapping
    public ResponseEntity<Calificacion> calificar(@Valid @RequestBody CalificacionRequest req) {
        return ResponseEntity.ok(service.calificar(req));
    }

    /** Lista las calificaciones del estudiante autenticado. */
    @GetMapping("/mias")
    public List<Calificacion> mias() {
        return service.calificacionesDe(UsuarioActual.email());
    }
}
