package com.optiplant.inventario.catalogo.mapper;

import com.optiplant.inventario.catalogo.dto.CategoriaResponse;
import com.optiplant.inventario.catalogo.entity.Categoria;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    CategoriaResponse toResponse(Categoria categoria);
}
