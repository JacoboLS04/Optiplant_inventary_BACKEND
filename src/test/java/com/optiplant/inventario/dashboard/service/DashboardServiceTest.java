package com.optiplant.inventario.dashboard.service;

import com.optiplant.inventario.catalogo.entity.Producto;
import com.optiplant.inventario.catalogo.entity.Sucursal;
import com.optiplant.inventario.catalogo.repository.SucursalRepository;
import com.optiplant.inventario.dashboard.dto.RotacionResponse;
import com.optiplant.inventario.dashboard.dto.VentasMensualesResponse;
import com.optiplant.inventario.inventario.entity.Existencia;
import com.optiplant.inventario.inventario.entity.MovimientoInventario;
import com.optiplant.inventario.inventario.repository.ExistenciaRepository;
import com.optiplant.inventario.inventario.repository.MovimientoInventarioRepository;
import com.optiplant.inventario.transferencias.repository.TransferenciaRepository;
import com.optiplant.inventario.ventas.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ExistenciaRepository existenciaRepository;
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private MovimientoInventarioRepository movimientoRepository;
    @Mock
    private VentaRepository ventaRepository;
    @Mock
    private TransferenciaRepository transferenciaRepository;

    @InjectMocks
    private DashboardService service;

    @Test
    void ventasMensualesConstruyeCuatroMesesRellenandoCeros() {
        YearMonth mesActual = YearMonth.now();
        LocalDateTime desde = mesActual.minusMonths(3).atDay(1).atStartOfDay();

        Object[] filaActual = new Object[]{
                mesActual.getYear(), mesActual.getMonthValue(), BigDecimal.valueOf(120)};
        Object[] filaHaceDos = new Object[]{
                mesActual.minusMonths(2).getYear(),
                mesActual.minusMonths(2).getMonthValue(),
                BigDecimal.valueOf(30)};

        when(ventaRepository.sumTotalPorMesDesde(any())).thenReturn(List.of(
                filaHaceDos, filaActual));

        VentasMensualesResponse result = service.ventasMensuales();

        assertEquals(4, result.getMesesConsiderados());
        assertEquals(4, result.getMeses().size());
        assertEquals(0, new BigDecimal("150").compareTo(result.getTotalPeriodo()));
        assertEquals(new BigDecimal("120"), result.getMeses().get(3).getTotal());
        assertEquals(new BigDecimal("30"), result.getMeses().get(1).getTotal());
        assertEquals(BigDecimal.ZERO, result.getMeses().get(2).getTotal());
        assertEquals(0, new BigDecimal("150").compareTo(result.getMeses().stream()
                .map(VentasMensualesResponse.MesVentas::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    @Test
    void rotacionAgrupaSalidasYCalculaStock() {
        Producto p = Producto.builder()
                .id(10L).sku("SKU-1").nombre("Producto A").build();
        Sucursal s = Sucursal.builder().id(1L).nombre("Sucursal Central").build();

        Existencia exist = Existencia.builder()
                .producto(p).sucursal(s)
                .cantidadFisica(BigDecimal.valueOf(10))
                .cantidadReservada(BigDecimal.ZERO)
                .build();

        MovimientoInventario retiro = MovimientoInventario.builder()
                .id(1L)
                .producto(p).sucursal(s)
                .tipo("retiro")
                .cantidad(BigDecimal.valueOf(5))
                .fecha(LocalDateTime.now().minusDays(1))
                .build();

        when(movimientoRepository.findAll()).thenReturn(List.of(retiro));
        when(existenciaRepository.findAll()).thenReturn(List.of(exist));

        RotacionResponse result = service.rotacion();

        assertEquals(new BigDecimal("5"), result.getTotalUnidades());
        assertEquals(1, result.getAltaDemanda().size());
        RotacionResponse.ProductoRotacion top = result.getAltaDemanda().get(0);
        assertEquals(10L, top.getProductoId());
        assertEquals(new BigDecimal("10"), top.getStockActual());
        assertEquals(0, new BigDecimal("0.5").compareTo(top.getRotacion()));
    }
}
