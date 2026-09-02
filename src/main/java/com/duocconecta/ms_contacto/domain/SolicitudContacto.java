package com.duocconecta.ms_contacto.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "solicitudes_contacto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** oid de quien solicita el contacto. */
    @Column(name = "solicitante_id", nullable = false, length = 100)
    private String solicitanteId;

    /** oid del alumno cuyo contacto se solicita; es quien debe aceptar o rechazar. */
    @Column(name = "solicitado_id", nullable = false, length = 100)
    private String solicitadoId;

    /** Publicación o repositorio que originó la solicitud (contexto para el destinatario). */
    @Column(name = "publicacion_id")
    private UUID publicacionId;

    @Column(name = "repositorio_id")
    private UUID repositorioId;

    @Column(length = 500)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

    /**
     * Lo que el solicitado eligió mostrar al aceptar (ej: correo, sección).
     * Se completa solo cuando estado = ACEPTADA; nunca se guarda antes de la aceptación explícita.
     */
    @Column(name = "datos_compartidos", length = 500)
    private String datosCompartidos;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private Instant fechaSolicitud;

    @Column(name = "fecha_respuesta")
    private Instant fechaRespuesta;

    @PrePersist
    void alPersistir() {
        this.fechaSolicitud = Instant.now();
        if (this.estado == null) {
            this.estado = EstadoSolicitud.PENDIENTE;
        }
    }
}
