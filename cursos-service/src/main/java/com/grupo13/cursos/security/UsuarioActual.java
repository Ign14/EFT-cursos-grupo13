package com.grupo13.cursos.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Utilidad para obtener la identidad (email/nombre) del usuario autenticado
 * desde el JWT de Azure AD B2C. B2C suele exponer el correo en el claim "emails".
 */
public final class UsuarioActual {

    private UsuarioActual() {}

    public static String email() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt jwt) {
            Object emails = jwt.getClaim("emails");
            if (emails instanceof List<?> l && !l.isEmpty()) {
                return String.valueOf(l.get(0));
            }
            String name = jwt.getClaimAsString("name");
            if (name != null && !name.isBlank()) return name;
            return jwt.getSubject();
        }
        return "desconocido";
    }
}
