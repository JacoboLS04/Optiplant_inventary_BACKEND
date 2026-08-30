package com.optiplant.inventario.inventario.service;

import com.optiplant.inventario.catalogo.entity.Producto;
import com.optiplant.inventario.catalogo.entity.Sucursal;
import com.optiplant.inventario.catalogo.repository.ProductoRepository;
import com.optiplant.inventario.catalogo.repository.SucursalRepository;
import com.optiplant.inventario.common.exception.BusinessRuleException;
import com.optiplant.inventario.common.exception.ResourceNotFoundException;
import com.optiplant.inventario.common.security.UsuarioActualService;
import com.optiplant.inventario.identidad.repository.UsuarioRepository;
import com.optiplant.inventario.inventario.dto.MovimientoInventarioRequest;
import com.optiplant.inventario.inventario.dto.MovimientoInventarioResponse;
import com.optiplant.inventario.inventario.entity.Existencia;
import com.optiplant.inventario.inventario.entity.MovimientoInventario;
import com.optiplant.inventario.inventario.mapper.MovimientoInventarioMapper;
import com.optiplant.inventario.inventario.repository.ExistenciaRepository;
import com.optiplant.inventario.inventario.repository.MovimientoInventarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimientoInventarioServiceTest {

    @Mock
    private MovimientoInventarioRepository repository;
    @Mock
    private ExistenciaRepository existenciaRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private MovimientoInventarioMapper mapper;
    @Mock
    private UsuarioActualService usuarioActualService;

    @InjectMocks
    private MovimientoInventarioService service;

    private Producto producto() {
        return Producto.builder().id(10L).sku("SKU-1").nombre("Producto A").build();
    }

    private Sucursal sucursal() {
        return Sucursal.builder().id(1L).nombre("Sucursal Central").build();
    }

    @Test
    void registrarMermaDescuentaStock() {
        Producto p = producto();
        Sucursal s = sucursal();
        Existencia exist = Existencia.builder()
                .producto(p).sucursal(s)
                .cantidadFisica(BigDecimal.valueOf(20))
                .cantidadReservada(BigDecimal.ZERO)
                .build();

        when(productoRepository.findById(10L)).thenReturn(Optional.of(p));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(s));
        when(existenciaRepository.findByProductoIdAndSucursalId(10L, 1L))
                .thenReturn(Optional.of(exist));
        when(repository.save(any(MovimientoInventario.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(MovimientoInventario.class))).thenReturn(
                new MovimientoInventarioResponse(1L, 10L, "SKU-1", "Producto A",
                        1L, "Sucursal Central", "merma", "deterioro",
                        BigDecimal.valueOf(5), null, null, null));

        MovimientoInventarioRequest request = new MovimientoInventarioRequest(
                10L, 1L, "merma", "deterioro", BigDecimal.valueOf(5), null);

        MovimientoInventarioResponse result = service.registrar(request);

        assertEquals(0, new BigDecimal("15").compareTo(exist.getCantidadFisica()));
        assertEquals(BigDecimal.valueOf(5), result.getCantidad());
        verify(existenciaRepository).save(exist);
    }

    @Test
    void registrarMermaRechazaStockInsuficiente() {
        Producto p = producto();
        Sucursal s = sucursal();
        Existencia exist = Existencia.builder()
                .producto(p).sucursal(s)
                .cantidadFisica(BigDecimal.valueOf(3))
                .cantidadReservada(BigDecimal.ZERO)
                .build();

        when(productoRepository.findById(10L)).thenReturn(Optional.of(p));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(s));
        when(existenciaRepository.findByProductoIdAndSucursalId(10L, 1L))
                .thenReturn(Optional.of(exist));

        MovimientoInventarioRequest request = new MovimientoInventarioRequest(
                10L, 1L, "merma", "deterioro", BigDecimal.valueOf(10), null);

        assertThrows(BusinessRuleException.class, () -> service.registrar(request));
    }

    @Test
    void registrarRechazaTipoInvalido() {
        Producto p = producto();
        Sucursal s = sucursal();
        when(productoRepository.findById(10L)).thenReturn(Optional.of(p));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(s));

        MovimientoInventarioRequest request = new MovimientoInventarioRequest(
                10L, 1L, "prestamo", "otro", BigDecimal.valueOf(1), null);

        assertThrows(BusinessRuleException.class, () -> service.registrar(request));
    }

    @Test
    void registrarLanzaAccessDeniedSiNoPuedeAccederASucursal() {
        Producto p = producto();
        Sucursal s = sucursal();
        when(productoRepository.findById(10L)).thenReturn(Optional.of(p));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(s));
        doThrow(new AccessDeniedException("RF-009"))
                .when(usuarioActualService).validarAccesoSucursal(1L);

        MovimientoInventarioRequest request = new MovimientoInventarioRequest(
                10L, 1L, "ingreso", "compra", BigDecimal.valueOf(1), null);

        assertThrows(AccessDeniedException.class, () -> service.registrar(request));
    }

    @Test
    void registrarLanzaResourceNotFoundSiProductoNoExiste() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        MovimientoInventarioRequest request = new MovimientoInventarioRequest(
                99L, 1L, "ingreso", "compra", BigDecimal.valueOf(1), null);

        assertThrows(ResourceNotFoundException.class, () -> service.registrar(request));
    }
}
