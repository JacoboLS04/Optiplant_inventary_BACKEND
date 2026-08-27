package com.optiplant.inventario.catalogo.mapper;

import com.optiplant.inventario.catalogo.dto.SucursalResponse;
import com.optiplant.inventario.catalogo.entity.Sucursal;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SucursalMapper {

    SucursalResponse toResponse(Sucursal sucursal);
}
