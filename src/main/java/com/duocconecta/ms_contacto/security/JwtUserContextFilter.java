package com.duocconecta.ms_contacto.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro propio que se ejecuta DESPUÉS de que Spring Security ya validó
 * la firma y el emisor del JWT contra Azure AD (spring-boot-starter-oauth2-resource-server).
 *
 * Acá se agrega una segunda capa de validación específica del negocio:
 *  - Verifica que el token traiga los claims esperados (oid, preferred_username).
 *  - Verifica que el correo pertenezca al dominio institucional.
 *  - Deja el id y correo del usuario disponibles para los controllers vía
 *    request.getAttribute(...), evitando repetir el parseo del JWT en cada endpoint.
 */
@Component
public class JwtUserContextFilter extends OncePerRequestFilter {

    @Value("${duocconecta.dominio-institucional:duocuc.cl}")
    private String dominioInstitucional;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Jwt jwt = jwtAuthenticationToken.getToken();

            String oid = jwt.getClaimAsString("oid");
            String correo = jwt.getClaimAsString("preferred_username");

            if (oid == null || correo == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token sin los claims esperados");
                return;
            }

            if (!correo.toLowerCase().endsWith("@" + dominioInstitucional.toLowerCase())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Dominio institucional no autorizado");
                return;
            }

            request.setAttribute("currentUserId", oid);
            request.setAttribute("currentUserEmail", correo);
        }

        filterChain.doFilter(request, response);
    }
}
