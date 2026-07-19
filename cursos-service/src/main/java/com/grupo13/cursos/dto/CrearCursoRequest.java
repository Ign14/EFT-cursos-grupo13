package com.grupo13.cursos.dto;

import jakarta.validation.constraints.*;

public record CrearCursoRequest(
        @NotBlank String codigo,
        @NotBlank String nombre,
        String descripcion,
        @NotNull @Min(1) Integer cupos) {}
