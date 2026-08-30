package com.optiplant.inventario.catalogo.service;

import com.optiplant.inventario.catalogo.dto.SucursalRequest;
import com.optiplant.inventario.catalogo.dto.SucursalResponse;
import com.optiplant.inventario.catalogo.entity.Sucursal;
import com.optiplant.inventario.catalogo.mapper.SucursalMapper;
import com.optiplant.inventario.catalogo.repository.SucursalRepository;
import com.optiplant.inventario.common.exception.BusinessRuleException;
import com.optiplant.inventario.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SucursalServiceTest {

    @Mock
    private SucursalRepository repository;
    @Mock
    private SucursalMapper mapper;

    @InjectMocks
    private SucursalService service;

    private Sucursal sucursal(Long id, String nombre) {
        return Sucursal.builder().id(id).nombre(nombre).build();
    }

    private SucursalResponse response(Long id, String nombre) {
        return new SucursalResponse(id, nombre, null, null);
    }

    @Test
    void createGuardaSucursalYDevuelveResponse() {
        Sucursal saved = sucursal(1L, "Sucursal Norte");
        when(repository.findAll()).thenReturn(List.of());
        when(repository.save(any(Sucursal.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response(1L, "Sucursal Norte"));

        SucursalResponse result = service.create(
                new SucursalRequest("Sucursal Norte", "Calle 1"));

        assertEquals(1L, result.getId());
        assertEquals("Sucursal Norte", result.getNombre());
        verify(repository).save(any(Sucursal.class));
    }

    @Test
    void createRechazaNombreDuplicado() {
        when(repository.findAll()).thenReturn(List.of(sucursal(1L, "Central")));

        assertThrows(BusinessRuleException.class,
                () -> service.create(new SucursalRequest("central", "Dir")));
    }

    @Test
    void updatePermiteConservarElMismoNombreExcluyendoElId() {
        Sucursal actual = sucursal(1L, "Central");
        when(repository.findById(1L)).thenReturn(Optional.of(actual));
        when(repository.findAll()).thenReturn(List.of(actual));
        when(repository.save(any(Sucursal.class))).thenReturn(actual);
        when(mapper.toResponse(actual)).thenReturn(response(1L, "Central Renovada"));

        SucursalResponse result = service.update(1L,
                new SucursalRequest("Central Renovada", "Nueva dir"));

        assertEquals("Central Renovada", result.getNombre());
    }

    @Test
    void inactivarMarcaEstadoInactiva() {
        Sucursal actual = sucursal(1L, "Central");
        when(repository.findById(1L)).thenReturn(Optional.of(actual));
        when(repository.save(any(Sucursal.class))).thenReturn(actual);
        when(mapper.toResponse(actual)).thenReturn(response(1L, "Central"));

        service.inactivar(1L);

        assertEquals("inactiva", actual.getEstado());
    }

    @Test
    void inactivarRechazaSiYaEstabaInactiva() {
        Sucursal inactiva = sucursal(1L, "Central");
        inactiva.setEstado("inactiva");
        when(repository.findById(1L)).thenReturn(Optional.of(inactiva));

        assertThrows(BusinessRuleException.class, () -> service.inactivar(1L));
    }

    @Test
    void getByIdLanzaResourceNotFoundExceptionSiNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L));
    }
}
