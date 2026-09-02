package com.duocconecta.ms_contacto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SolicitudRequestDTO(

        @NotBlank(message = "El id del usuario solicitado es obligatorio")
        String solicitadoId,

        UUID publicacionId,

        UUID repositorioId,

        @Size(max = 500)
        String mensaje
) {
}