package com.grupo13.cursos.service;

import com.grupo13.cursos.dto.*;
import com.grupo13.cursos.entity.*;
import com.grupo13.cursos.exception.*;
import com.grupo13.cursos.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class CursoService {

    private final CursoRepository cursoRepo;
    private final InscripcionRepository inscripcionRepo;
    private final CalificacionRepository calificacionRepo;
    private final S3StorageService s3;

    public CursoService(CursoRepository cursoRepo, InscripcionRepository inscripcionRepo,
                        CalificacionRepository calificacionRepo, S3StorageService s3) {
        this.cursoRepo = cursoRepo;
        this.inscripcionRepo = inscripcionRepo;
        this.calificacionRepo = calificacionRepo;
        this.s3 = s3;
    }

    // ---------- Cursos ----------
    public Curso crear(CrearCursoRequest req, String instructor) {
        cursoRepo.findByCodigo(req.codigo()).ifPresent(c -> {
            throw new ReglaNegocioException("Ya existe un curso con codigo " + req.codigo());
        });
        return cursoRepo.save(Curso.builder()
                .codigo(req.codigo()).nombre(req.nombre()).descripcion(req.descripcion())
                .cupos(req.cupos()).instructor(instructor).creadoEn(Instant.now())
                .build());
    }

    public List<Curso> listar() { return cursoRepo.findAll(); }

    public Curso obtener(Long id) {
        return cursoRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado: " + id));
    }

    public Curso actualizar(Long id, ActualizarCursoRequest req) {
        Curso c = obtener(id);
        if (req.nombre() != null) c.setNombre(req.nombre());
        if (req.descripcion() != null) c.setDescripcion(req.descripcion());
        if (req.cupos() != null) c.setCupos(req.cupos());
        return cursoRepo.save(c);
    }

    public void eliminar(Long id) {
        Curso c = obtener(id);
        cursoRepo.delete(c);
    }

    // ---------- Material en S3 ----------
    public Curso subirMaterial(Long id, MultipartFile archivo) {
        Curso c = obtener(id);
        String nombre = (archivo.getOriginalFilename() != null) ? archivo.getOriginalFilename() : "material";
        String key = "cursos/" + c.getCodigo() + "/material/" + nombre;
        try {
            s3.subirBytes(key, archivo.getBytes(), archivo.getContentType());
        } catch (IOException e) {
            throw new AlmacenamientoException("No se pudo leer el archivo de material", e);
        }
        c.setMaterialS3Key(key);
        return cursoRepo.save(c);
    }

    public byte[] descargarMaterial(Long id) {
        Curso c = obtener(id);
        if (c.getMaterialS3Key() == null) {
            throw new RecursoNoEncontradoException("El curso " + id + " no tiene material cargado");
        }
        return s3.descargar(c.getMaterialS3Key());
    }

    // ---------- Inscripciones ----------
    public Inscripcion inscribir(Long cursoId, String estudiante) {
        Curso c = obtener(cursoId);
        if (inscripcionRepo.existsByCursoIdAndEstudiante(cursoId, estudiante)) {
            throw new ReglaNegocioException("El estudiante ya esta inscrito en el curso " + cursoId);
        }
        long inscritos = inscripcionRepo.findByCursoId(cursoId).size();
        if (c.getCupos() != null && inscritos >= c.getCupos()) {
            throw new ReglaNegocioException("El curso " + cursoId + " no tiene cupos disponibles");
        }
        return inscripcionRepo.save(Inscripcion.builder()
                .cursoId(cursoId).estudiante(estudiante)
                .fecha(LocalDate.now()).estado("INSCRITO").build());
    }

    public List<Inscripcion> inscripcionesDe(String estudiante) {
        return inscripcionRepo.findByEstudiante(estudiante);
    }

    public List<Inscripcion> inscripcionesDeCurso(Long cursoId) {
        return inscripcionRepo.findByCursoId(cursoId);
    }

    // ---------- Calificaciones ----------
    public Calificacion calificar(CalificacionRequest req) {
        obtener(req.cursoId()); // valida que el curso exista
        return calificacionRepo.save(Calificacion.builder()
                .cursoId(req.cursoId()).estudiante(req.estudiante()).examen(req.examen())
                .nota(req.nota()).fecha(LocalDate.now()).build());
    }

    public List<Calificacion> calificacionesDe(String estudiante) {
        return calificacionRepo.findByEstudiante(estudiante);
    }
}
