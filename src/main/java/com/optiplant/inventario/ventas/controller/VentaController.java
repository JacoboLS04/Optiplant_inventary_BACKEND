package com.optiplant.inventario.ventas.controller;

import com.optiplant.inventario.common.dto.PaginatedResponse;
import com.optiplant.inventario.ventas.dto.NuevaVentaRequest;
import com.optiplant.inventario.ventas.dto.ProductoVentaResponse;
import com.optiplant.inventario.ventas.dto.VentaResponse;
import com.optiplant.inventario.ventas.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService service;

    @GetMapping("/catalogo")
    public ResponseEntity<List<ProductoVentaResponse>> catalogo() {
        return ResponseEntity.ok(service.catalogo());
    }

    @PostMapping
    public ResponseEntity<VentaResponse> registrar(
            @Valid @RequestBody NuevaVentaRequest request) {
        VentaResponse response = service.registrar(request);
        return ResponseEntity.created(
                        URI.create("/api/v1/ventas/" + response.getId()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<VentaResponse>> buscar(
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.buscar(
                sucursalId, fechaDesde, fechaHasta, busqueda, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }
}
