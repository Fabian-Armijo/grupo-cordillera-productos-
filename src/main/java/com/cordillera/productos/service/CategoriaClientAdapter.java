package com.cordillera.productos.service;

import com.cordillera.productos.Client.CategoriaClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CategoriaClientAdapter {

    @Autowired
    private CategoriaClient categoriaClient;

    @CircuitBreaker(name = "categoriaCB", fallbackMethod = "fallbackValidarCategoria")
    public void validarCategoria(Long categoriaId) {
        categoriaClient.obtenerCategoriaPorId(categoriaId);
    }

    public void fallbackValidarCategoria(Long categoriaId, Throwable excepcion) {
        System.out.println("CIRCUIT BREAKER ACTIVADO: El servicio de Categorías falló o el ID no existe. Excepción: " + excepcion.getMessage());
    }
}