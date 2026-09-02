package com.duocconecta.ms_contacto.dto;

import jakarta.validation.constraints.NotNull;

public record RespuestaSolicitudDTO(

        @NotNull(message = "Debes indicar si aceptas la solicitud")
        boolean aceptar,

        /**
         * Solo se usa si aceptar = true. Lo que el usuario decide mostrar
         * (por ejemplo: "correo: xxx@duocuc.cl, sección: DSY1107-002D").
         * Si acepta sin especificar nada, no se comparte ningún dato adicional.
         */
        String datosAMostrar
) {
}
