package com.grupo13.cursos.dto;

import jakarta.validation.constraints.NotNull;

public record InscripcionRequest(@NotNull Long cursoId) {}
