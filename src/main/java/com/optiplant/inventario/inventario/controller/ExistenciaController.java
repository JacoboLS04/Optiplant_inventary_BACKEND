package com.optiplant.inventario.inventario.controller;

import com.optiplant.inventario.common.dto.PaginatedResponse;
import com.optiplant.inventario.inventario.dto.ExistenciaResponse;
import com.optiplant.inventario.inventario.dto.ExistenciaUpdateRequest;
import com.optiplant.inventario.inventario.service.ExistenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/existencias")
@RequiredArgsConstructor
public class ExistenciaController {

    private final ExistenciaService service;

    @GetMapping
    public ResponseEntity<PaginatedResponse<ExistenciaResponse>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) String estadoStock,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime actualizadoDesde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime actualizadoHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(service.search(
                search, categoriaId, sucursalId, estadoStock,
                actualizadoDesde, actualizadoHasta, page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExistenciaResponse> updateStockMinimo(
            @PathVariable Long id,
            @Valid @RequestBody ExistenciaUpdateRequest request) {
        return ResponseEntity.ok(service.updateStockMinimo(id, request));
    }
}
