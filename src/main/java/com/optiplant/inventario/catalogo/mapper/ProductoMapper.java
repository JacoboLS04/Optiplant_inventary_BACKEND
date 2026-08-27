package com.optiplant.inventario.catalogo.mapper;

import com.optiplant.inventario.catalogo.dto.ProductoResponse;
import com.optiplant.inventario.catalogo.entity.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    @Mapping(source = "categoria.id", target = "categoriaId")
    @Mapping(source = "categoria.nombre", target = "categoriaNombre")
    @Mapping(source = "unidadBase.id", target = "unidadBaseId")
    ProductoResponse toResponse(Producto producto);
}
