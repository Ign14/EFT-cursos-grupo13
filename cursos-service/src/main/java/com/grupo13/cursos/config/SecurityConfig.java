package com.grupo13.cursos.config;

import com.grupo13.cursos.security.RolesClaimConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Seguridad del cursos-service (EFT S9).
 * Resource Server OAuth2 que valida los JWT de Azure AD B2C (IDaaS) y autoriza
 * por rol (INSTRUCTOR / ESTUDIANTE) leido del claim extension_Role.
 */
@Configuration
public class SecurityConfig {

    public static final String INSTRUCTOR = "INSTRUCTOR";
    public static final String ESTUDIANTE = "ESTUDIANTE";

    @Value("${azure.b2c.jwk-set-uri:}")
    private String jwkSetUri;

    @Value("${azure.b2c.issuer:}")
    private String issuer;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationConverter jwtAuthConverter) throws Exception {
        http
            .cors(c -> {})
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Descargar material del curso: estudiante o instructor
                .requestMatchers(HttpMethod.GET, "/api/cursos/*/material").hasAnyRole(ESTUDIANTE, INSTRUCTOR)
                // Subir material y gestionar cursos: instructor
                .requestMatchers(HttpMethod.POST, "/api/cursos/*/material").hasRole(INSTRUCTOR)
                .requestMatchers(HttpMethod.POST, "/api/cursos").hasRole(INSTRUCTOR)
                .requestMatchers(HttpMethod.PUT, "/api/cursos/*").hasRole(INSTRUCTOR)
                .requestMatchers(HttpMethod.DELETE, "/api/cursos/*").hasRole(INSTRUCTOR)
                // Inscribirse: estudiante
                .requestMatchers(HttpMethod.POST, "/api/inscripciones").hasRole(ESTUDIANTE)
                // Calificar: instructor
                .requestMatchers(HttpMethod.POST, "/api/calificaciones").hasRole(INSTRUCTOR)
                // El resto (listar/ver) requiere autenticacion (cualquier rol)
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(RolesClaimConverter rolesConverter) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(rolesConverter);
        return converter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String uri = (jwkSetUri != null && !jwkSetUri.isBlank())
                ? jwkSetUri
                : "https://login.microsoftonline.com/common/discovery/v2.0/keys";
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(uri).build();
        if (issuer != null && !issuer.isBlank()) {
            OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
            decoder.setJwtValidator(withIssuer);
        }
        return decoder;
    }

    /** CORS para permitir que el frontend (SPA) consuma la API. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}
