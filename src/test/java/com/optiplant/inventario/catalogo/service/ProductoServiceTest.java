package com.optiplant.inventario.catalogo.service;

import com.optiplant.inventario.catalogo.dto.ProductoRequest;
import com.optiplant.inventario.catalogo.dto.ProductoResponse;
import com.optiplant.inventario.catalogo.entity.Categoria;
import com.optiplant.inventario.catalogo.entity.Producto;
import com.optiplant.inventario.catalogo.entity.UnidadMedida;
import com.optiplant.inventario.catalogo.mapper.ProductoMapper;
import com.optiplant.inventario.catalogo.repository.ProductoRepository;
import com.optiplant.inventario.common.exception.BusinessRuleException;
import com.optiplant.inventario.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repository;
    @Mock
    private ProductoMapper mapper;
    @Mock
    private CategoriaService categoriaService;
    @Mock
    private UnidadMedidaService unidadMedidaService;

    @InjectMocks
    private ProductoService service;

    private Categoria categoria() {
        return Categoria.builder().id(1L).nombre("Lacteos").build();
    }

    private UnidadMedida unidad() {
        return UnidadMedida.builder().id(2L).nombre("Unidad").simbolo("u").build();
    }

    private Producto producto(Long id, String sku, String nombre) {
        return Producto.builder()
                .id(id).sku(sku).nombre(nombre)
                .categoria(categoria())
                .unidadBase(unidad())
                .build();
    }

    private ProductoResponse response(Long id, String nombre) {
        return new ProductoResponse(id, "SKU", nombre, null,
                1L, "Lacteos", 2L, "activo");
    }

    @Test
    void inactivarMarcaElProductoComoInactivo() {
        Producto p = producto(10L, "SKU-1", "Queso");
        when(repository.findById(10L)).thenReturn(Optional.of(p));
        when(repository.save(any(Producto.class))).thenReturn(p);
        when(mapper.toResponse(p)).thenReturn(response(10L, "Queso"));

        service.inactivar(10L);

        assertEquals("inactivo", p.getEstado());
        verify(repository).save(p);
    }

    @Test
    void inactivarRechazaSiYaEstabaInactivo() {
        Producto p = producto(10L, "SKU-1", "Queso");
        p.setEstado("inactivo");
        when(repository.findById(10L)).thenReturn(Optional.of(p));

        assertThrows(BusinessRuleException.class, () -> service.inactivar(10L));
    }

    @Test
    void inactivarLanzaResourceNotFoundSiNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.inactivar(99L));
    }

    @Test
    void getByIdLanzaResourceNotFoundSiNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void updateRechazaSkuDuplicado() {
        Producto p = producto(10L, "SKU-1", "Queso");
        when(repository.findById(10L)).thenReturn(Optional.of(p));
        when(categoriaService.findById(1L)).thenReturn(categoria());
        when(unidadMedidaService.findById(2L)).thenReturn(unidad());
        when(repository.existsBySku("SKU-2")).thenReturn(true);

        ProductoRequest request = new ProductoRequest("Queso", null, 1L, 2L, "SKU-2");

        assertThrows(BusinessRuleException.class, () -> service.update(10L, request));
    }

    @Test
    void createGuardaProductoConSkuExplicito() {
        Producto p = producto(10L, "SKU-1", "Queso");
        when(categoriaService.findById(1L)).thenReturn(categoria());
        when(unidadMedidaService.findById(2L)).thenReturn(unidad());
        when(repository.save(any(Producto.class))).thenReturn(p);
        when(mapper.toResponse(p)).thenReturn(response(10L, "Queso"));

        ProductoRequest request = new ProductoRequest("Queso", null, 1L, 2L, "SKU-1");

        ProductoResponse result = service.create(request);

        assertEquals("Queso", result.getNombre());
        verify(repository).save(any(Producto.class));
    }
}
