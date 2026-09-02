package com.duocconecta.ms_contacto.service;

import com.duocconecta.ms_contacto.domain.EstadoSolicitud;
import com.duocconecta.ms_contacto.domain.SolicitudContacto;
import com.duocconecta.ms_contacto.dto.RespuestaSolicitudDTO;
import com.duocconecta.ms_contacto.dto.SolicitudRequestDTO;
import com.duocconecta.ms_contacto.exception.OperacionNoPermitidaException;
import com.duocconecta.ms_contacto.exception.RecursoNoEncontradoException;
import com.duocconecta.ms_contacto.repository.SolicitudContactoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SolicitudContactoService {

    private final SolicitudContactoRepository solicitudContactoRepository;

    public SolicitudContacto crear(SolicitudRequestDTO dto, String solicitanteId) {
        if (solicitanteId.equals(dto.solicitadoId())) {
            throw new OperacionNoPermitidaException("No puedes solicitar contacto contigo mismo");
        }

        solicitudContactoRepository.findBySolicitanteIdAndSolicitadoIdAndEstado(
                        solicitanteId, dto.solicitadoId(), EstadoSolicitud.PENDIENTE)
                .ifPresent(s -> {
                    throw new OperacionNoPermitidaException("Ya existe una solicitud pendiente a este usuario");
                });

        SolicitudContacto solicitud = SolicitudContacto.builder()
                .solicitanteId(solicitanteId)
                .solicitadoId(dto.solicitadoId())
                .publicacionId(dto.publicacionId())
                .repositorioId(dto.repositorioId())
                .mensaje(dto.mensaje())
                .estado(EstadoSolicitud.PENDIENTE)
                .build();

        SolicitudContacto guardada = solicitudContactoRepository.save(solicitud);

        // TODO (Evaluación N°3): publicar evento "solicitud.creada" en RabbitMQ
        // para que ms-notificaciones avise por correo al solicitado. Por ahora
        // el aviso se resuelve consultando el endpoint de "recibidas".

        return guardada;
    }

    /**
     * Responde una solicitud. Solo puede hacerlo el usuario solicitado (dueño del consentimiento).
     * Los datos de contacto SOLO se guardan si acepta explícitamente.
     */
    public SolicitudContacto responder(UUID id, String usuarioId, RespuestaSolicitudDTO respuesta) {
        SolicitudContacto solicitud = solicitudContactoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada: " + id));

        if (!solicitud.getSolicitadoId().equals(usuarioId)) {
            throw new OperacionNoPermitidaException("Solo el usuario solicitado puede responder esta solicitud");
        }
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new OperacionNoPermitidaException("Esta solicitud ya fue respondida");
        }

        if (respuesta.aceptar()) {
            solicitud.setEstado(EstadoSolicitud.ACEPTADA);
            solicitud.setDatosCompartidos(respuesta.datosAMostrar());
            // TODO (Evaluación N°3): publicar evento "solicitud.aceptada" en RabbitMQ
            // para que ms-notificaciones envíe los datos por correo al solicitante.
        } else {
            solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        }
        solicitud.setFechaRespuesta(Instant.now());

        return solicitudContactoRepository.save(solicitud);
    }

    public List<SolicitudContacto> recibidas(String usuarioId) {
        return solicitudContactoRepository.findBySolicitadoId(usuarioId);
    }

    public List<SolicitudContacto> enviadas(String usuarioId) {
        return solicitudContactoRepository.findBySolicitanteId(usuarioId);
    }
}
