package com.optiplant.inventario.identidad.service;

import com.optiplant.inventario.catalogo.entity.Sucursal;
import com.optiplant.inventario.catalogo.repository.SucursalRepository;
import com.optiplant.inventario.common.exception.BusinessRuleException;
import com.optiplant.inventario.identidad.dto.CrearUsuarioRequest;
import com.optiplant.inventario.identidad.dto.UsuarioResponse;
import com.optiplant.inventario.identidad.entity.Usuario;
import com.optiplant.inventario.identidad.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    private CrearUsuarioRequest request(String rol, Long sucursalId) {
        CrearUsuarioRequest r = new CrearUsuarioRequest();
        r.setEmail("usuario@optiplant.com");
        r.setPassword("secreto123");
        r.setNombre("Usuario Test");
        r.setRol(rol);
        r.setSucursalId(sucursalId);
        return r;
    }

    @Test
    void crearRechazaEmailDuplicado() {
        when(usuarioRepository.existsByEmail("usuario@optiplant.com")).thenReturn(true);

        assertThrows(BusinessRuleException.class,
                () -> service.crear(request("OPERADOR", 1L)));
    }

    @Test
    void crearRolInvalidoRechaza() {
        assertThrows(BusinessRuleException.class,
                () -> service.crear(request("SUPERADMIN", 1L)));
    }

    @Test
    void crearGerenteSinSucursalRechaza() {
        assertThrows(BusinessRuleException.class,
                () -> service.crear(request("GERENTE", null)));
    }

    @Test
    void crearAdministradorConSucursalRechaza() {
        assertThrows(BusinessRuleException.class,
                () -> service.crear(request("ADMINISTRADOR", 1L)));
    }

    @Test
    void crearOperadorExitosoAsignaSucursalYEncriptaPassword() {
        Sucursal sucursal = Sucursal.builder().id(2L).nombre("Sucursal Norte").build();

        when(usuarioRepository.existsByEmail("usuario@optiplant.com")).thenReturn(false);
        when(sucursalRepository.findById(2L)).thenReturn(Optional.of(sucursal));
        when(passwordEncoder.encode("secreto123")).thenReturn("hash-encrypted");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(9L);
            return u;
        });

        UsuarioResponse response = service.crear(request("OPERADOR", 2L));

        assertEquals(9L, response.getId());
        assertEquals("OPERADOR", response.getRol());
        assertEquals(2L, response.getSucursalId());
        assertEquals("Sucursal Norte", response.getSucursalNombre());
        assertTrue(response.getActivo());
    }
}
