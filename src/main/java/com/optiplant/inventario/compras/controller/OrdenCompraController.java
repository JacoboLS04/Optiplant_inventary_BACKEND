package com.optiplant.inventario.compras.controller;

import com.optiplant.inventario.common.dto.PaginatedResponse;
import com.optiplant.inventario.compras.dto.OrdenCompraEstadoRequest;
import com.optiplant.inventario.compras.dto.OrdenCompraRecepcionRequest;
import com.optiplant.inventario.compras.dto.OrdenCompraRequest;
import com.optiplant.inventario.compras.dto.OrdenCompraResponse;
import com.optiplant.inventario.compras.entity.EstadoOrdenCompra;
import com.optiplant.inventario.compras.service.OrdenCompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/ordenes-compra")
@RequiredArgsConstructor
public class OrdenCompraController {

    private final OrdenCompraService service;

    @PostMapping
    public ResponseEntity<OrdenCompraResponse> crear(
            @Valid @RequestBody OrdenCompraRequest request) {
        OrdenCompraResponse response = service.crear(request);
        return ResponseEntity.created(
                        URI.create("/api/v1/ordenes-compra/" + response.getId()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<OrdenCompraResponse>> buscar(
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) EstadoOrdenCompra estado,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Long productoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                service.buscar(sucursalId, estado, busqueda, productoId, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenCompraResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping("/{id}/estado")
    public ResponseEntity<OrdenCompraResponse> cambiarEstado(
            @PathVariable Long id, @Valid @RequestBody OrdenCompraEstadoRequest request) {
        return ResponseEntity.ok(service.cambiarEstado(id, request));
    }

    @PostMapping("/{id}/recepcion")
    public ResponseEntity<OrdenCompraResponse> recibir(
            @PathVariable Long id, @Valid @RequestBody OrdenCompraRecepcionRequest request) {
        return ResponseEntity.ok(service.recibir(id, request));
    }
}
