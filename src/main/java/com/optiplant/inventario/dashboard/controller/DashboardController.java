package com.optiplant.inventario.dashboard.controller;

import com.optiplant.inventario.dashboard.dto.BranchNetworkResponse;
import com.optiplant.inventario.dashboard.dto.InventoryMovementResponse;
import com.optiplant.inventario.dashboard.dto.InventorySummaryResponse;
import com.optiplant.inventario.dashboard.dto.RotacionResponse;
import com.optiplant.inventario.dashboard.dto.VentasMensualesResponse;
import com.optiplant.inventario.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/resumen")
    public ResponseEntity<InventorySummaryResponse> resumen() {
        return ResponseEntity.ok(service.resumen());
    }

    @GetMapping("/movimientos")
    public ResponseEntity<List<InventoryMovementResponse>> movimientos() {
        return ResponseEntity.ok(service.movimientos());
    }

    @GetMapping("/red")
    public ResponseEntity<BranchNetworkResponse> red() {
        return ResponseEntity.ok(service.red());
    }

    @GetMapping("/rotacion")
    public ResponseEntity<RotacionResponse> rotacion() {
        return ResponseEntity.ok(service.rotacion());
    }

    @GetMapping("/ventas-mensuales")
    public ResponseEntity<VentasMensualesResponse> ventasMensuales() {
        return ResponseEntity.ok(service.ventasMensuales());
    }
}
