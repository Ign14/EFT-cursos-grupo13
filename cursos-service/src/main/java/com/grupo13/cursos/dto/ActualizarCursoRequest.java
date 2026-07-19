package com.grupo13.cursos.dto;

public record ActualizarCursoRequest(
        String nombre,
        String descripcion,
        Integer cupos) {}
