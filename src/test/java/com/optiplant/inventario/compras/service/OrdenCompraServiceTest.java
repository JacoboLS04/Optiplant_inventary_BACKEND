package com.optiplant.inventario.compras.service;

import com.optiplant.inventario.catalogo.repository.ProductoRepository;
import com.optiplant.inventario.catalogo.repository.SucursalRepository;
import com.optiplant.inventario.common.exception.BusinessRuleException;
import com.optiplant.inventario.common.exception.ResourceNotFoundException;
import com.optiplant.inventario.compras.dto.OrdenCompraEstadoRequest;
import com.optiplant.inventario.compras.dto.OrdenCompraResponse;
import com.optiplant.inventario.compras.entity.EstadoOrdenCompra;
import com.optiplant.inventario.compras.entity.OrdenCompra;
import com.optiplant.inventario.compras.mapper.ComprasMapper;
import com.optiplant.inventario.compras.repository.OrdenCompraRepository;
import com.optiplant.inventario.compras.repository.ProveedorRepository;
import com.optiplant.inventario.identidad.repository.UsuarioRepository;
import com.optiplant.inventario.inventario.repository.ExistenciaRepository;
import com.optiplant.inventario.inventario.service.MovimientoInventarioService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenCompraServiceTest {

    @Mock
    private OrdenCompraRepository ordenCompraRepository;
    @Mock
    private ProveedorRepository proveedorRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ExistenciaRepository existenciaRepository;
    @Mock
    private MovimientoInventarioService movimientoInventarioService;
    @Mock
    private ComprasMapper mapper;

    @InjectMocks
    private OrdenCompraService service;

    private OrdenCompra orden(EstadoOrdenCompra estado) {
        return OrdenCompra.builder()
                .id(1L)
                .codigo("PO-001")
                .estado(estado)
                .lineas(new ArrayList<>())
                .build();
    }

    private Specification<OrdenCompra> capturarSpecification() {
        ArgumentCaptor<Specification<OrdenCompra>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(ordenCompraRepository).findAll(captor.capture(), any(Pageable.class));
        return captor.getValue();
    }

    @Test
    void buscarConProductoIdAplicaJoinDistinctSobreLineas() throws Exception {
        when(ordenCompraRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.buscar(null, null, null, 42L, 0, 20);

        Specification<OrdenCompra> spec = capturarSpecification();

        Root<OrdenCompra> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Join<Object, Object> lineas = mock(Join.class);
        Path<Object> producto = mock(Path.class);
        Path<Object> id = mock(Path.class);
        when(root.join("lineas")).thenReturn(lineas);
        when(lineas.get("producto")).thenReturn(producto);
        when(producto.get("id")).thenReturn(id);
        when(cb.equal(id, 42L)).thenReturn(mock(Predicate.class));
        when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        spec.toPredicate(root, query, cb);

        verify(query).distinct(true);
        verify(root).join("lineas");
    }

    @Test
    void buscarSinProductoIdNoAplicaDistinct() throws Exception {
        when(ordenCompraRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.buscar(null, null, null, null, 0, 20);

        Specification<OrdenCompra> spec = capturarSpecification();

        Root<OrdenCompra> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        spec.toPredicate(root, query, cb);

        verify(query, never()).distinct(true);
    }

    @Test
    void cambiarEstadoPermiteTransicionValidaDeBorradorAEnviada() {
        OrdenCompra orden = orden(EstadoOrdenCompra.BORRADOR);
        when(ordenCompraRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenReturn(orden);

        OrdenCompraResponse response = OrdenCompraResponse.builder()
                .id(1L).codigo("PO-001").estado(EstadoOrdenCompra.ENVIADA).build();
        when(mapper.toResponse(orden)).thenReturn(response);

        OrdenCompraResponse result = service.cambiarEstado(1L,
                new OrdenCompraEstadoRequest(EstadoOrdenCompra.ENVIADA));

        assertEquals(EstadoOrdenCompra.ENVIADA, result.getEstado());
        assertEquals(EstadoOrdenCompra.ENVIADA, orden.getEstado());
    }

    @Test
    void cambiarEstadoRechazaTransicionInvalida() {
        OrdenCompra orden = orden(EstadoOrdenCompra.BORRADOR);
        when(ordenCompraRepository.findById(1L)).thenReturn(Optional.of(orden));

        assertThrows(BusinessRuleException.class, () -> service.cambiarEstado(1L,
                new OrdenCompraEstadoRequest(EstadoOrdenCompra.RECIBIDA)));
    }

    @Test
    void obtenerLanzaResourceNotFoundSiNoExiste() {
        when(ordenCompraRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obtener(99L));
    }
}
