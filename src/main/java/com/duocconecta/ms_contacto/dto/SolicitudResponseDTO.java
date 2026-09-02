package com.duocconecta.ms_contacto.dto;

import com.duocconecta.ms_contacto.domain.EstadoSolicitud;
import com.duocconecta.ms_contacto.domain.SolicitudContacto;

import java.time.Instant;
import java.util.UUID;

public record SolicitudResponseDTO(
        UUID id,
        String solicitanteId,
        String solicitadoId,
        UUID publicacionId,
        UUID repositorioId,
        String mensaje,
        EstadoSolicitud estado,
        String datosCompartidos,
        Instant fechaSolicitud,
        Instant fechaRespuesta
) {
    public static SolicitudResponseDTO desdeEntidad(SolicitudContacto s) {
        return new SolicitudResponseDTO(
                s.getId(), s.getSolicitanteId(), s.getSolicitadoId(), s.getPublicacionId(),
                s.getRepositorioId(), s.getMensaje(), s.getEstado(), s.getDatosCompartidos(),
                s.getFechaSolicitud(), s.getFechaRespuesta());
    }
}
