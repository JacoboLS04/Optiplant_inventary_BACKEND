package com.optiplant.inventario.compras.controller;

import com.optiplant.inventario.compras.dto.DespachoRequest;
import com.optiplant.inventario.compras.dto.OrdenCompraResponse;
import com.optiplant.inventario.compras.entity.EstadoOrdenCompra;
import com.optiplant.inventario.compras.service.OrdenCompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/portal/ordenes-compra")
@RequiredArgsConstructor
public class PortalProveedorController {

    private final OrdenCompraService service;

    @GetMapping
    public ResponseEntity<List<OrdenCompraResponse>> listar(
            @RequestParam(required = false) EstadoOrdenCompra estado,
            @RequestParam(required = false) String busqueda) {
        return ResponseEntity.ok(service.listarParaProveedor(estado, busqueda));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenCompraResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerParaProveedor(id));
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<OrdenCompraResponse> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(service.confirmar(id));
    }

    @PostMapping("/{id}/despachar")
    public ResponseEntity<OrdenCompraResponse> despachar(
            @PathVariable Long id, @RequestBody(required = false) DespachoRequest request) {
        return ResponseEntity.ok(service.despachar(id, request != null ? request : new DespachoRequest()));
    }
}