package com.cordillera.productos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductoResponseDTO {
    private Long id;
    private String sku;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Long categoriaId;
    private String nombreCategoria;
    private Long sucursalId; // Agrega esta línea para que el builder pueda armar la respuesta hacia el Frontend
    private Boolean activo;
}