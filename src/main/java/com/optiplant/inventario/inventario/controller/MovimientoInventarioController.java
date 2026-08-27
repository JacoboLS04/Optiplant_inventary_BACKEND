package com.optiplant.inventario.inventario.controller;

import com.optiplant.inventario.common.dto.PaginatedResponse;
import com.optiplant.inventario.inventario.dto.MovimientoInventarioRequest;
import com.optiplant.inventario.inventario.dto.MovimientoInventarioResponse;
import com.optiplant.inventario.inventario.service.MovimientoInventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/movimientos-inventario")
@RequiredArgsConstructor
public class MovimientoInventarioController {

    private final MovimientoInventarioService service;

    @PostMapping
    public ResponseEntity<MovimientoInventarioResponse> registrar(
            @Valid @RequestBody MovimientoInventarioRequest request) {
        MovimientoInventarioResponse response = service.registrar(request);
        return ResponseEntity.created(
                        URI.create("/api/v1/movimientos-inventario/" + response.getId()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<MovimientoInventarioResponse>> search(
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.search(productoId, sucursalId, page, size));
    }
}
