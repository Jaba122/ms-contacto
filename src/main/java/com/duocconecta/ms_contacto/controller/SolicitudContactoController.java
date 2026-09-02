package com.duocconecta.ms_contacto.controller;

import com.duocconecta.ms_contacto.domain.SolicitudContacto;
import com.duocconecta.ms_contacto.dto.RespuestaSolicitudDTO;
import com.duocconecta.ms_contacto.dto.SolicitudRequestDTO;
import com.duocconecta.ms_contacto.dto.SolicitudResponseDTO;
import com.duocconecta.ms_contacto.service.SolicitudContactoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contacto/solicitudes")
@RequiredArgsConstructor
public class SolicitudContactoController {

    private final SolicitudContactoService solicitudContactoService;

    private String usuarioActual(HttpServletRequest request) {
        return (String) request.getAttribute("currentUserId");
    }

    @PostMapping
    public ResponseEntity<SolicitudResponseDTO> crear(@Valid @RequestBody SolicitudRequestDTO dto,
                                                        HttpServletRequest request) {
        SolicitudContacto creada = solicitudContactoService.crear(dto, usuarioActual(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(SolicitudResponseDTO.desdeEntidad(creada));
    }

    @PatchMapping("/{id}/responder")
    public ResponseEntity<SolicitudResponseDTO> responder(@PathVariable UUID id,
                                                            @Valid @RequestBody RespuestaSolicitudDTO respuesta,
                                                            HttpServletRequest request) {
        SolicitudContacto actualizada = solicitudContactoService.responder(id, usuarioActual(request), respuesta);
        return ResponseEntity.ok(SolicitudResponseDTO.desdeEntidad(actualizada));
    }

    @GetMapping("/recibidas")
    public ResponseEntity<List<SolicitudResponseDTO>> recibidas(HttpServletRequest request) {
        List<SolicitudResponseDTO> resultado = solicitudContactoService.recibidas(usuarioActual(request))
                .stream().map(SolicitudResponseDTO::desdeEntidad).toList();
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/enviadas")
    public ResponseEntity<List<SolicitudResponseDTO>> enviadas(HttpServletRequest request) {
        List<SolicitudResponseDTO> resultado = solicitudContactoService.enviadas(usuarioActual(request))
                .stream().map(SolicitudResponseDTO::desdeEntidad).toList();
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("ms-contacto activo");
    }
}
