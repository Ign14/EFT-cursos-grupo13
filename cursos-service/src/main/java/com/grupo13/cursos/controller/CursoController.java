package com.grupo13.cursos.controller;

import com.grupo13.cursos.dto.*;
import com.grupo13.cursos.entity.Curso;
import com.grupo13.cursos.entity.Inscripcion;
import com.grupo13.cursos.security.UsuarioActual;
import com.grupo13.cursos.service.CursoService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

/** Gestion de cursos y su material (almacenamiento Cloud S3). */
@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService service;

    public CursoController(CursoService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Curso> crear(@Valid @RequestBody CrearCursoRequest req) {
        Curso c = service.crear(req, UsuarioActual.email());
        return ResponseEntity.created(URI.create("/api/cursos/" + c.getId())).body(c);
    }

    @GetMapping
    public List<Curso> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public Curso detalle(@PathVariable Long id) { return service.obtener(id); }

    @PutMapping("/{id}")
    public Curso actualizar(@PathVariable Long id, @RequestBody ActualizarCursoRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Inscritos de un curso (instructor/estudiante autenticado)
    @GetMapping("/{id}/inscripciones")
    public List<Inscripcion> inscritos(@PathVariable Long id) {
        return service.inscripcionesDeCurso(id);
    }

    // ---- Material del curso en S3 ----
    @PostMapping(value = "/{id}/material", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Curso subirMaterial(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo) {
        return service.subirMaterial(id, archivo);
    }

    @GetMapping("/{id}/material")
    public ResponseEntity<byte[]> descargarMaterial(@PathVariable Long id) {
        byte[] contenido = service.descargarMaterial(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"material-curso-" + id + "\"")
                .body(contenido);
    }
}
