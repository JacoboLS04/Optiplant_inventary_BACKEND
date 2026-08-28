package com.optiplant.inventario.compras.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 200)
    private String contacto;

    @Column(columnDefinition = "TEXT")
    private String condicionesGenerales;
}
