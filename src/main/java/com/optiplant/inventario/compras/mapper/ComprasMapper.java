package com.optiplant.inventario.compras.mapper;

import com.optiplant.inventario.compras.dto.OrdenCompraRequest;
import com.optiplant.inventario.compras.dto.OrdenCompraResponse;
import com.optiplant.inventario.compras.dto.ProveedorRequest;
import com.optiplant.inventario.compras.dto.ProveedorResponse;
import com.optiplant.inventario.compras.entity.OrdenCompra;
import com.optiplant.inventario.compras.entity.OrdenCompraLinea;
import com.optiplant.inventario.compras.entity.Proveedor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ComprasMapper {

    Proveedor toEntity(ProveedorRequest request);

    @Mapping(target = "id", ignore = true)
    void updateProveedor(ProveedorRequest request, @MappingTarget Proveedor proveedor);

    ProveedorResponse toResponse(Proveedor proveedor);

    List<ProveedorResponse> toProveedorResponseList(List<Proveedor> proveedores);

    @Mapping(target = "proveedor", ignore = true)
    @Mapping(target = "sucursalDestino", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaEmision", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lineas", ignore = true)
    OrdenCompra toEntity(OrdenCompraRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "proveedorId", source = "proveedor.id")
    @Mapping(target = "nombreProveedor", source = "proveedor.nombre")
    @Mapping(target = "sucursalDestinoId", source = "sucursalDestino.id")
    @Mapping(target = "nombreSucursal", source = "sucursalDestino.nombre")
    @Mapping(target = "usuarioId", expression = "java(o.getUsuario() != null ? o.getUsuario().getId() : null)")
    @Mapping(target = "nombreUsuario", expression = "java(o.getUsuario() != null ? o.getUsuario().getNombre() : null)")
    @Mapping(target = "lineas", expression = "java(toLineaResponseList(o))")
    @Mapping(target = "total", expression = "java(calcularTotal(o))")
    @Mapping(target = "estado", source = "estado")
    OrdenCompraResponse toResponse(OrdenCompra o);

    default List<OrdenCompraResponse.LineaResponse> toLineaResponseList(OrdenCompra o) {
        return o.getLineas().stream().map(this::toLineaResponse).toList();
    }

    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "sku", source = "producto.sku")
    @Mapping(target = "nombreProducto", source = "producto.nombre")
    @Mapping(target = "subtotal", expression = "java(calcularSubtotal(l))")
    OrdenCompraResponse.LineaResponse toLineaResponse(OrdenCompraLinea l);

    default BigDecimal calcularSubtotal(OrdenCompraLinea l) {
        BigDecimal neto = l.getPrecioUnitario().subtract(l.getDescuento());
        if (neto.compareTo(BigDecimal.ZERO) < 0) {
            neto = BigDecimal.ZERO;
        }
        return l.getCantidadRecibida().multiply(neto);
    }

    default BigDecimal calcularTotal(OrdenCompra o) {
        return o.getLineas().stream()
                .map(this::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
