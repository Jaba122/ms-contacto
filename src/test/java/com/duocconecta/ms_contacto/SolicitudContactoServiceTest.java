package com.duocconecta.ms_contacto;

import com.duocconecta.ms_contacto.domain.EstadoSolicitud;
import com.duocconecta.ms_contacto.domain.SolicitudContacto;
import com.duocconecta.ms_contacto.dto.RespuestaSolicitudDTO;
import com.duocconecta.ms_contacto.dto.SolicitudRequestDTO;
import com.duocconecta.ms_contacto.exception.OperacionNoPermitidaException;
import com.duocconecta.ms_contacto.repository.SolicitudContactoRepository;
import com.duocconecta.ms_contacto.service.SolicitudContactoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudContactoServiceTest {

    @Mock
    private SolicitudContactoRepository solicitudContactoRepository;

    @InjectMocks
    private SolicitudContactoService solicitudContactoService;

    private final String solicitanteId = "usuario-a";
    private final String solicitadoId = "usuario-b";

    @Test
    void crear_deberiaRechazarSolicitudASiMismo() {
        SolicitudRequestDTO dto = new SolicitudRequestDTO(solicitanteId, null, null, "hola");

        assertThatThrownBy(() -> solicitudContactoService.crear(dto, solicitanteId))
                .isInstanceOf(OperacionNoPermitidaException.class);

        verify(solicitudContactoRepository, never()).save(any());
    }

    @Test
    void crear_deberiaRechazarSiYaHaySolicitudPendiente() {
        SolicitudRequestDTO dto = new SolicitudRequestDTO(solicitadoId, null, null, "hola");

        when(solicitudContactoRepository.findBySolicitanteIdAndSolicitadoIdAndEstado(
                solicitanteId, solicitadoId, EstadoSolicitud.PENDIENTE))
                .thenReturn(Optional.of(mock(SolicitudContacto.class)));

        assertThatThrownBy(() -> solicitudContactoService.crear(dto, solicitanteId))
                .isInstanceOf(OperacionNoPermitidaException.class);
    }

    @Test
    void responder_alAceptar_deberiaGuardarSoloLosDatosQueElUsuarioEligioMostrar() {
        UUID id = UUID.randomUUID();
        SolicitudContacto solicitud = SolicitudContacto.builder()
                .id(id)
                .solicitanteId(solicitanteId)
                .solicitadoId(solicitadoId)
                .estado(EstadoSolicitud.PENDIENTE)
                .build();

        when(solicitudContactoRepository.findById(id)).thenReturn(Optional.of(solicitud));
        when(solicitudContactoRepository.save(any(SolicitudContacto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RespuestaSolicitudDTO respuesta = new RespuestaSolicitudDTO(true, "correo: b@duocuc.cl");
        SolicitudContacto resultado = solicitudContactoService.responder(id, solicitadoId, respuesta);

        assertThat(resultado.getEstado()).isEqualTo(EstadoSolicitud.ACEPTADA);
        assertThat(resultado.getDatosCompartidos()).isEqualTo("correo: b@duocuc.cl");
        assertThat(resultado.getFechaRespuesta()).isNotNull();
    }

    @Test
    void responder_alRechazar_noDeberiaGuardarNingunDatoDeContacto() {
        UUID id = UUID.randomUUID();
        SolicitudContacto solicitud = SolicitudContacto.builder()
                .id(id)
                .solicitanteId(solicitanteId)
                .solicitadoId(solicitadoId)
                .estado(EstadoSolicitud.PENDIENTE)
                .build();

        when(solicitudContactoRepository.findById(id)).thenReturn(Optional.of(solicitud));
        when(solicitudContactoRepository.save(any(SolicitudContacto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RespuestaSolicitudDTO respuesta = new RespuestaSolicitudDTO(false, null);
        SolicitudContacto resultado = solicitudContactoService.responder(id, solicitadoId, respuesta);

        assertThat(resultado.getEstado()).isEqualTo(EstadoSolicitud.RECHAZADA);
        assertThat(resultado.getDatosCompartidos()).isNull();
    }

    @Test
    void responder_deberiaRechazarSiQuienRespondeNoEsElSolicitado() {
        UUID id = UUID.randomUUID();
        SolicitudContacto solicitud = SolicitudContacto.builder()
                .id(id)
                .solicitanteId(solicitanteId)
                .solicitadoId(solicitadoId)
                .estado(EstadoSolicitud.PENDIENTE)
                .build();

        when(solicitudContactoRepository.findById(id)).thenReturn(Optional.of(solicitud));

        RespuestaSolicitudDTO respuesta = new RespuestaSolicitudDTO(true, "correo: falso@duocuc.cl");

        assertThatThrownBy(() -> solicitudContactoService.responder(id, "usuario-intruso", respuesta))
                .isInstanceOf(OperacionNoPermitidaException.class);

        verify(solicitudContactoRepository, never()).save(any());
    }

    @Test
    void responder_deberiaRechazarSiLaSolicitudYaFueRespondida() {
        UUID id = UUID.randomUUID();
        SolicitudContacto solicitud = SolicitudContacto.builder()
                .id(id)
                .solicitanteId(solicitanteId)
                .solicitadoId(solicitadoId)
                .estado(EstadoSolicitud.ACEPTADA)
                .build();

        when(solicitudContactoRepository.findById(id)).thenReturn(Optional.of(solicitud));

        RespuestaSolicitudDTO respuesta = new RespuestaSolicitudDTO(false, null);

        assertThatThrownBy(() -> solicitudContactoService.responder(id, solicitadoId, respuesta))
                .isInstanceOf(OperacionNoPermitidaException.class);
    }
}
