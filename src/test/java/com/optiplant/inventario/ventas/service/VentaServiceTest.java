package com.optiplant.inventario.ventas.service;

import com.optiplant.inventario.catalogo.entity.Categoria;
import com.optiplant.inventario.catalogo.entity.Producto;
import com.optiplant.inventario.catalogo.entity.Sucursal;
import com.optiplant.inventario.common.exception.BusinessRuleException;
import com.optiplant.inventario.common.security.UsuarioActualService;
import org.springframework.security.access.AccessDeniedException;
import com.optiplant.inventario.inventario.entity.Existencia;
import com.optiplant.inventario.inventario.repository.ExistenciaRepository;
import com.optiplant.inventario.inventario.service.MovimientoInventarioService;
import com.optiplant.inventario.ventas.dto.NuevaVentaRequest;
import com.optiplant.inventario.ventas.dto.VentaResponse;
import com.optiplant.inventario.ventas.entity.Venta;
import com.optiplant.inventario.ventas.mapper.VentaMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private com.optiplant.inventario.ventas.repository.VentaRepository ventaRepository;
    @Mock
    private ExistenciaRepository existenciaRepository;
    @Mock
    private com.optiplant.inventario.catalogo.repository.SucursalRepository sucursalRepository;
    @Mock
    private com.optiplant.inventario.identidad.repository.UsuarioRepository usuarioRepository;
    @Mock
    private MovimientoInventarioService movimientoInventarioService;
    @Mock
    private com.optiplant.inventario.catalogo.service.PrecioService precioService;
    @Mock
    private VentaMapper mapper;
    @Mock
    private UsuarioActualService usuarioActualService;

    @InjectMocks
    private VentaService service;

    private Sucursal sucursal() {
        return Sucursal.builder().id(1L).nombre("Sucursal Central").build();
    }

    private Producto producto() {
        return Producto.builder()
                .id(10L)
                .sku("SKU-1")
                .nombre("Producto A")
                .categoria(Categoria.builder().id(1L).nombre("Cat").build())
                .build();
    }

    private Existencia existencia(Sucursal s, Producto p, BigDecimal disponible) {
        return Existencia.builder()
                .producto(p)
                .sucursal(s)
                .cantidadFisica(disponible)
                .cantidadReservada(BigDecimal.ZERO)
                .costoPromedio(BigDecimal.valueOf(10))
                .build();
    }

    @Test
    void registrarAplicaDescuentoYDescuentaStock() {
        Sucursal s = sucursal();
        Producto p = producto();

        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(s));
        when(existenciaRepository.findByProductoIdAndSucursalId(10L, 1L))
                .thenReturn(Optional.of(existencia(s, p, BigDecimal.valueOf(20))));
        when(precioService.obtenerPrecioGlobal(10L)).thenReturn(BigDecimal.valueOf(100));
        when(ventaRepository.countByCodigoStartingWith("VENTA-")).thenReturn(0L);
        when(ventaRepository.findByCodigo("VENTA-0001")).thenReturn(Optional.empty());
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            return VentaResponse.builder()
                    .id(1L)
                    .codigo(v.getCodigo())
                    .sucursalId(v.getSucursal().getId())
                    .total(v.getTotal())
                    .unidades(v.getUnidades())
                    .build();
        });

        NuevaVentaRequest request = NuevaVentaRequest.builder()
                .sucursalId(1L)
                .descuentoPorcentaje(BigDecimal.valueOf(10))
                .lineas(List.of(NuevaVentaRequest.LineaRequest.builder()
                        .productoId(10L).cantidad(BigDecimal.valueOf(2)).build()))
                .build();

        VentaResponse response = service.registrar(request);

        assertEquals(new BigDecimal("180.00"), response.getTotal());
        assertEquals(new BigDecimal("2"), response.getUnidades());
        verify(ventaRepository).save(any(Venta.class));
        verify(movimientoInventarioService).registrar(any());
    }

    @Test
    void registrarRechazaStockInsuficiente() {
        Sucursal s = sucursal();
        Producto p = producto();

        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(s));
        when(existenciaRepository.findByProductoIdAndSucursalId(10L, 1L))
                .thenReturn(Optional.of(existencia(s, p, BigDecimal.valueOf(5))));

        NuevaVentaRequest request = NuevaVentaRequest.builder()
                .sucursalId(1L)
                .descuentoPorcentaje(BigDecimal.ZERO)
                .lineas(List.of(NuevaVentaRequest.LineaRequest.builder()
                        .productoId(10L).cantidad(BigDecimal.valueOf(99)).build()))
                .build();

        assertThrows(BusinessRuleException.class, () -> service.registrar(request));
    }

    @Test
    void registrarRechazaAccesoASucursalAjena() {
        Sucursal s = sucursal();
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(s));
        doThrow(new AccessDeniedException("RF-009"))
                .when(usuarioActualService).validarAccesoSucursal(1L);

        NuevaVentaRequest request = NuevaVentaRequest.builder()
                .sucursalId(1L)
                .descuentoPorcentaje(BigDecimal.ZERO)
                .lineas(List.of(NuevaVentaRequest.LineaRequest.builder()
                        .productoId(10L).cantidad(BigDecimal.valueOf(1)).build()))
                .build();

        assertThrows(AccessDeniedException.class, () -> service.registrar(request));
    }
}
