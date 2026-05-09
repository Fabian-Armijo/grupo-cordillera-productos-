package com.cordillera.productos.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-categorias", url = "http://localhost:8083/api/categorias")
public interface CategoriaClient {

    // Llama al GET /api/categorias/{id} del otro microservicio
    @GetMapping("/{id}")
    Object obtenerCategoriaPorId(@PathVariable("id") Long id);
}
