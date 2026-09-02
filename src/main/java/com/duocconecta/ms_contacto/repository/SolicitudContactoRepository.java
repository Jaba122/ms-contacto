package com.duocconecta.ms_contacto.repository;

import com.duocconecta.ms_contacto.domain.EstadoSolicitud;
import com.duocconecta.ms_contacto.domain.SolicitudContacto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SolicitudContactoRepository extends JpaRepository<SolicitudContacto, UUID> {

    List<SolicitudContacto> findBySolicitadoId(String solicitadoId);

    List<SolicitudContacto> findBySolicitanteId(String solicitanteId);

    Optional<SolicitudContacto> findBySolicitanteIdAndSolicitadoIdAndEstado(
            String solicitanteId, String solicitadoId, EstadoSolicitud estado);
}
