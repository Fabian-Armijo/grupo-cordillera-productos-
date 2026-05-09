package com.cordillera.productos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductoRequestDTO {
    @NotBlank(message = "El SKU es obligatorio")
    private String sku;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Positive
    private Double precio;

    @NotNull(message = "El costo es obligatorio")
    @Positive
    private Double costo;

    @NotNull(message = "El ID de categoría es obligatorio")
    private Long categoriaId;

    private Boolean activo;
}
