package com.optiplant.inventario.transferencias.controller;

import com.optiplant.inventario.common.dto.PaginatedResponse;
import com.optiplant.inventario.transferencias.dto.TransferenciaAprobacionRequest;
import com.optiplant.inventario.transferencias.dto.TransferenciaDespachoRequest;
import com.optiplant.inventario.transferencias.dto.TransferenciaRecepcionRequest;
import com.optiplant.inventario.transferencias.dto.TransferenciaRequest;
import com.optiplant.inventario.transferencias.dto.TransferenciaResponse;
import com.optiplant.inventario.transferencias.entity.EstadoTransferencia;
import com.optiplant.inventario.transferencias.service.TransferenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/transferencias")
@RequiredArgsConstructor
public class TransferenciaController {

    private final TransferenciaService service;

    @PostMapping
    public ResponseEntity<TransferenciaResponse> crear(
            @Valid @RequestBody TransferenciaRequest request) {
        TransferenciaResponse response = service.crear(request);
        return ResponseEntity.created(
                        URI.create("/api/v1/transferencias/" + response.getId()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<TransferenciaResponse>> buscar(
            @RequestParam(required = false) Long sucursalOrigenId,
            @RequestParam(required = false) Long sucursalDestinoId,
            @RequestParam(required = false) EstadoTransferencia estado,
            @RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.buscar(
                sucursalOrigenId, sucursalDestinoId, estado, busqueda, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferenciaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping("/{id}/aprobacion")
    public ResponseEntity<TransferenciaResponse> aprobar(
            @PathVariable Long id,
            @Valid @RequestBody TransferenciaAprobacionRequest request) {
        return ResponseEntity.ok(service.aprobar(id, request));
    }

    @PostMapping("/{id}/preparacion")
    public ResponseEntity<TransferenciaResponse> preparar(@PathVariable Long id) {
        return ResponseEntity.ok(service.preparar(id));
    }

    @PostMapping("/{id}/despacho")
    public ResponseEntity<TransferenciaResponse> despachar(
            @PathVariable Long id,
            @Valid @RequestBody TransferenciaDespachoRequest request) {
        return ResponseEntity.ok(service.despachar(id, request));
    }

    @PostMapping("/{id}/recepcion")
    public ResponseEntity<TransferenciaResponse> recibir(
            @PathVariable Long id,
            @Valid @RequestBody TransferenciaRecepcionRequest request) {
        return ResponseEntity.ok(service.recibir(id, request));
    }

    @PostMapping("/{id}/cancelacion")
    public ResponseEntity<TransferenciaResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelar(id));
    }
}
