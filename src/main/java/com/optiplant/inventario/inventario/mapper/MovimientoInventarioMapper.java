package com.optiplant.inventario.inventario.mapper;

import com.optiplant.inventario.inventario.dto.MovimientoInventarioResponse;
import com.optiplant.inventario.inventario.entity.MovimientoInventario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MovimientoInventarioMapper {

    @Mapping(source = "producto.id", target = "productoId")
    @Mapping(source = "producto.sku", target = "sku")
    @Mapping(source = "producto.nombre", target = "nombreProducto")
    @Mapping(source = "sucursal.id", target = "sucursalId")
    @Mapping(source = "sucursal.nombre", target = "nombreSucursal")
    @Mapping(source = "usuario.id", target = "usuarioId")
    @Mapping(source = "usuario.nombre", target = "nombreUsuario")
    MovimientoInventarioResponse toResponse(MovimientoInventario movimiento);
}
