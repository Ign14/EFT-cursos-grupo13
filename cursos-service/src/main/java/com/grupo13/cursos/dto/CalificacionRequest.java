package com.grupo13.cursos.dto;

import jakarta.validation.constraints.*;

public record CalificacionRequest(
        @NotNull Long cursoId,
        @NotBlank String estudiante,
        @NotBlank String examen,
        @NotNull @DecimalMin("1.0") @DecimalMax("7.0") Double nota) {}
