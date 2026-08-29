package com.optiplant.inventario.dashboard.service;

import com.optiplant.inventario.catalogo.entity.Sucursal;
import com.optiplant.inventario.catalogo.repository.SucursalRepository;
import com.optiplant.inventario.dashboard.dto.BranchNetworkResponse;
import com.optiplant.inventario.dashboard.dto.InventoryMovementResponse;
import com.optiplant.inventario.dashboard.dto.InventorySummaryResponse;
import com.optiplant.inventario.dashboard.dto.RotacionResponse;
import com.optiplant.inventario.dashboard.dto.VentasMensualesResponse;
import com.optiplant.inventario.inventario.entity.Existencia;
import com.optiplant.inventario.inventario.entity.MovimientoInventario;
import com.optiplant.inventario.inventario.repository.ExistenciaRepository;
import com.optiplant.inventario.inventario.repository.MovimientoInventarioRepository;
import com.optiplant.inventario.transferencias.entity.Transferencia;
import com.optiplant.inventario.transferencias.repository.TransferenciaRepository;
import com.optiplant.inventario.ventas.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final BigDecimal CERO = BigDecimal.ZERO;

    private final ExistenciaRepository existenciaRepository;
    private final SucursalRepository sucursalRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final VentaRepository ventaRepository;
    private final TransferenciaRepository transferenciaRepository;

    @Transactional(readOnly = true)
    public InventorySummaryResponse resumen() {
        List<Existencia> existencias = existenciaRepository.findAll();
        List<Sucursal> sucursales = sucursalRepository.findByEstado("activa");

        BigDecimal totalValue = CERO;
        BigDecimal totalUnits = CERO;
        long skuCount = existencias.stream()
                .map(e -> e.getProducto().getId())
                .distinct().count();
        long branchCount = sucursales.size();

        Map<String, BigDecimal[]> porCategoria = new LinkedHashMap<>();
        for (Existencia e : existencias) {
            BigDecimal cantidad = e.getCantidadFisica();
            BigDecimal valor = cantidad
                    .multiply(e.getCostoPromedio() != null ? e.getCostoPromedio() : CERO);
            totalUnits = totalUnits.add(cantidad);
            totalValue = totalValue.add(valor);

            String nombre = e.getProducto().getCategoria() != null
                    ? e.getProducto().getCategoria().getNombre() : "Sin categoría";
            BigDecimal[] acc = porCategoria.computeIfAbsent(nombre,
                    k -> new BigDecimal[]{CERO, CERO});
            acc[0] = acc[0].add(cantidad);
            acc[1] = acc[1].add(valor);
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime hace30 = ahora.minusDays(30);
        LocalDateTime hace60 = ahora.minusDays(60);

        BigDecimal inflow = CERO;
        for (MovimientoInventario m : movimientoRepository.findAll()) {
            if (m.getFecha() != null && !m.getFecha().isBefore(hace30)
                    && "ingreso".equals(m.getTipo())
                    && esCompra(m.getMotivo())) {
                BigDecimal costo = m.getProducto() != null
                        ? costoPromedio(m.getProducto().getId(), m.getSucursal().getId())
                        : CERO;
                inflow = inflow.add(m.getCantidad().multiply(costo));
            }
        }

        BigDecimal outflow = ventaRepository.sumTotalDesde(hace30);
        BigDecimal outflowAnterior = ventaRepository.sumTotalEntre(hace60, hace30);

        BigDecimal changePercent = CERO;
        if (outflowAnterior.compareTo(CERO) > 0) {
            changePercent = outflow.subtract(outflowAnterior)
                    .divide(outflowAnterior, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);
        }

        List<InventorySummaryResponse.CategoryDistribution> distribution =
                porCategoria.entrySet().stream()
                        .map(en -> InventorySummaryResponse.CategoryDistribution.builder()
                                .category(en.getKey())
                                .units(en.getValue()[0])
                                .value(en.getValue()[1])
                                .build())
                        .toList();

        InventorySummaryResponse.Summary summary = InventorySummaryResponse.Summary.builder()
                .totalValue(totalValue)
                .totalUnits(totalUnits)
                .skuCount(skuCount)
                .branchCount(branchCount)
                .inflowValue30d(inflow)
                .outflowValue30d(outflow)
                .changePercent(changePercent)
                .updatedAt(ahora)
                .build();

        return InventorySummaryResponse.builder()
                .summary(summary)
                .distribution(distribution)
                .build();
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> movimientos() {
        return movimientoRepository.findTop20ByOrderByFechaDesc().stream()
                .map(this::toMovementResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BranchNetworkResponse red() {
        List<Sucursal> sucursales = sucursalRepository.findByEstado("activa");
        List<Existencia> existencias = existenciaRepository.findAll();

        Map<Long, List<Existencia>> porSucursal = new LinkedHashMap<>();
        for (Existencia e : existencias) {
            porSucursal.computeIfAbsent(e.getSucursal().getId(), k -> new ArrayList<>())
                    .add(e);
        }

        List<BranchNetworkResponse.BranchNode> nodes = new ArrayList<>();
        int index = 0;
        for (Sucursal s : sucursales) {
            List<Existencia> deSucursal = porSucursal.getOrDefault(
                    s.getId(), List.of());
            BigDecimal units = deSucursal.stream()
                    .map(Existencia::getCantidadFisica)
                    .reduce(CERO, BigDecimal::add);
            long skuCount = deSucursal.stream()
                    .map(e -> e.getProducto().getId())
                    .distinct().count();
            long low = deSucursal.stream()
                    .filter(e -> e.getCantidadFisica()
                            .subtract(e.getCantidadReservada())
                            .compareTo(e.getStockMinimo()) < 0)
                    .count();

            String status = "ok";
            if (deSucursal.stream().anyMatch(e -> esCritico(e))) {
                status = "critical";
            } else if (low > 0) {
                status = "low";
            }

            BigDecimal x = BigDecimal.valueOf(20 + (index * 22) % 61);
            BigDecimal y = BigDecimal.valueOf(20 + ((index * 17) % 61));

            nodes.add(BranchNetworkResponse.BranchNode.builder()
                    .id(s.getId())
                    .name(s.getNombre())
                    .kind(esBodega(s.getNombre()) ? "warehouse" : "branch")
                    .status(status)
                    .units(units)
                    .skuCount(skuCount)
                    .lowStockCount(low)
                    .x(x)
                    .y(y)
                    .build());
            index++;
        }

        List<BranchNetworkResponse.BranchLink> links = new ArrayList<>();
        Map<String, BranchNetworkResponse.BranchLink> vistos = new LinkedHashMap<>();
        for (Transferencia t : transferenciaRepository.findAll()) {
            Long from = t.getSucursalOrigen().getId();
            Long to = t.getSucursalDestino().getId();
            String key = from + "->" + to;
            if (!vistos.containsKey(key)) {
                String status = nodes.stream()
                        .filter(n -> n.getId().equals(to))
                        .map(BranchNetworkResponse.BranchNode::getStatus)
                        .findFirst().orElse("ok");
                vistos.put(key, BranchNetworkResponse.BranchLink.builder()
                        .from(from).to(to).status(status).build());
            }
        }
        links.addAll(vistos.values());

        List<BranchNetworkResponse.StockAlert> alerts = new ArrayList<>();
        long nAlert = 1;
        for (Existencia e : existencias) {
            BigDecimal disponible = e.getCantidadFisica().subtract(e.getCantidadReservada());
            BigDecimal minimo = e.getStockMinimo();
            if (disponible.compareTo(minimo) < 0) {
                alerts.add(BranchNetworkResponse.StockAlert.builder()
                        .id(nAlert++)
                        .product(e.getProducto().getNombre())
                        .branchId(e.getSucursal().getId())
                        .branchName(e.getSucursal().getNombre())
                        .currentUnits(disponible)
                        .minUnits(minimo)
                        .severity(esCritico(e) ? "critical" : "low")
                        .build());
            }
        }

        return BranchNetworkResponse.builder()
                .nodes(nodes)
                .links(links)
                .alerts(alerts)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public RotacionResponse rotacion() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime desde = ahora.minusDays(30);

        Map<Long, BigDecimal> unidadesPorProducto = new LinkedHashMap<>();

        for (MovimientoInventario m : movimientoRepository.findAll()) {
            if (m.getFecha() == null || m.getProducto() == null) {
                continue;
            }
            Long pid = m.getProducto().getId();
            if ("retiro".equals(m.getTipo())
                    && !m.getFecha().isBefore(desde)) {
                unidadesPorProducto.merge(pid, m.getCantidad(), BigDecimal::add);
            }
        }

        Map<Long, BigDecimal> stockPorProducto = new LinkedHashMap<>();
        for (Existencia e : existenciaRepository.findAll()) {
            stockPorProducto.merge(e.getProducto().getId(),
                    e.getCantidadFisica().subtract(e.getCantidadReservada()),
                    BigDecimal::add);
        }

        Map<Long, String> skuPorProducto = new LinkedHashMap<>();
        Map<Long, String> nombrePorProducto = new LinkedHashMap<>();
        for (Existencia e : existenciaRepository.findAll()) {
            skuPorProducto.putIfAbsent(e.getProducto().getId(), e.getProducto().getSku());
            nombrePorProducto.putIfAbsent(e.getProducto().getId(), e.getProducto().getNombre());
        }

        List<RotacionResponse.ProductoRotacion> items = new ArrayList<>();
        for (Long pid : unidadesPorProducto.keySet()) {
            BigDecimal unidades = unidadesPorProducto.get(pid);
            BigDecimal stock = stockPorProducto.getOrDefault(pid, CERO);
            BigDecimal rotacion = stock.compareTo(CERO) > 0
                    ? unidades.divide(stock, 4, RoundingMode.HALF_UP)
                    : CERO;
            items.add(RotacionResponse.ProductoRotacion.builder()
                    .productoId(pid)
                    .sku(skuPorProducto.getOrDefault(pid, "—"))
                    .nombre(nombrePorProducto.getOrDefault(pid, "—"))
                    .unidades(unidades)
                    .stockActual(stock)
                    .rotacion(rotacion)
                    .build());
        }

        List<RotacionResponse.ProductoRotacion> alta = items.stream()
                .sorted(Comparator
                        .comparing(RotacionResponse.ProductoRotacion::getUnidades)
                        .reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<RotacionResponse.ProductoRotacion> baja = stockPorProducto.entrySet().stream()
                .filter(en -> en.getValue().compareTo(CERO) > 0
                        && unidadesPorProducto.getOrDefault(en.getKey(), CERO).compareTo(CERO) == 0)
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .map(en -> RotacionResponse.ProductoRotacion.builder()
                        .productoId(en.getKey())
                        .sku(skuPorProducto.getOrDefault(en.getKey(), "—"))
                        .nombre(nombrePorProducto.getOrDefault(en.getKey(), "—"))
                        .unidades(CERO)
                        .stockActual(en.getValue())
                        .rotacion(CERO)
                        .build())
                .limit(5)
                .collect(Collectors.toList());

        BigDecimal totalUnidades = unidadesPorProducto.values().stream()
                .reduce(CERO, BigDecimal::add);

        return RotacionResponse.builder()
                .periodoDias(30)
                .totalUnidades(totalUnidades)
                .altaDemanda(alta)
                .bajaDemanda(baja)
                .updatedAt(ahora)
                .build();
    }

    @Transactional(readOnly = true)
    public VentasMensualesResponse ventasMensuales() {
        int cantMeses = 4;
        LocalDateTime ahora = LocalDateTime.now();
        YearMonth mesActual = YearMonth.from(ahora);
        LocalDateTime desde = mesActual.minusMonths(cantMeses - 1)
                .atDay(1).atStartOfDay();

        Map<Integer, BigDecimal> porMesClave = new HashMap<>();
        for (Object[] row : ventaRepository.sumTotalPorMesDesde(desde)) {
            int anio = ((Number) row[0]).intValue();
            int mes = ((Number) row[1]).intValue();
            BigDecimal total = (BigDecimal) row[2];
            porMesClave.put(anio * 100 + mes, total);
        }

        List<VentasMensualesResponse.MesVentas> meses = new ArrayList<>();
        BigDecimal totalPeriodo = CERO;
        for (int i = cantMeses - 1; i >= 0; i--) {
            YearMonth ym = mesActual.minusMonths(i);
            BigDecimal total = porMesClave.getOrDefault(
                    ym.getYear() * 100 + ym.getMonthValue(), CERO);
            totalPeriodo = totalPeriodo.add(total);
            String abrev = ym.getMonth().getDisplayName(
                    TextStyle.SHORT, new Locale("es", "ES"));
            meses.add(VentasMensualesResponse.MesVentas.builder()
                    .anio(ym.getYear())
                    .mes(ym.getMonthValue())
                    .etiqueta(abrev + " " + ym.getYear())
                    .total(total)
                    .build());
        }

        return VentasMensualesResponse.builder()
                .mesesConsiderados(cantMeses)
                .totalPeriodo(totalPeriodo)
                .meses(meses)
                .updatedAt(ahora)
                .build();
    }

    private InventoryMovementResponse toMovementResponse(MovimientoInventario m) {        String type = "ingreso".equals(m.getTipo()) ? "entrada" : "salida";
        String motivo = m.getMotivo() != null ? m.getMotivo().toLowerCase() : "";
        if (motivo.contains("transferencia")) {
            type = "transferencia";
        } else if (motivo.contains("ajuste")) {
            type = "ajuste";
        }

        BigDecimal quantity = m.getCantidad();
        if ("salida".equals(type) || "transferencia".equals(type)) {
            quantity = quantity.negate();
        }

        return InventoryMovementResponse.builder()
                .id(m.getId())
                .product(m.getProducto().getNombre())
                .sku(m.getProducto().getSku())
                .type(type)
                .branch(m.getSucursal().getNombre())
                .quantity(quantity)
                .date(m.getFecha())
                .build();
    }

    private BigDecimal costoPromedio(Long productoId, Long sucursalId) {
        return existenciaRepository
                .findByProductoIdAndSucursalId(productoId, sucursalId)
                .map(Existencia::getCostoPromedio)
                .orElse(CERO);
    }

    private boolean esCompra(String motivo) {
        if (motivo == null) {
            return false;
        }
        return motivo.toLowerCase().contains("compra");
    }

    private boolean esBodega(String nombre) {
        if (nombre == null) {
            return false;
        }
        String n = nombre.toLowerCase();
        return n.contains("bodega") || n.contains("central") || n.contains("almacén");
    }

    private boolean esCritico(Existencia e) {
        BigDecimal disponible = e.getCantidadFisica().subtract(e.getCantidadReservada());
        BigDecimal minimo = e.getStockMinimo();
        if (minimo.compareTo(CERO) == 0) {
            return false;
        }
        return disponible.compareTo(CERO) == 0
                || disponible.divide(minimo, 4, RoundingMode.HALF_UP)
                        .compareTo(BigDecimal.valueOf(0.5)) <= 0;
    }
}
