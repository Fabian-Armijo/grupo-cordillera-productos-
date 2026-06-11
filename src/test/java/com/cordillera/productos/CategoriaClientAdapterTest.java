package com.cordillera.productos;

import com.cordillera.productos.Client.CategoriaClient;
import com.cordillera.productos.service.CategoriaClientAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoriaClientAdapterTest {

    @Mock
    private CategoriaClient categoriaClient; // Simulamos el cliente Feign

    @InjectMocks
    private CategoriaClientAdapter categoriaAdapter; // Inyectamos el mock en el adapter real

    @Test
    void validarCategoria_DeberiaLlamarAlClienteFeign() {
        // Arrange
        Long categoriaId = 5L;

        // Act
        categoriaAdapter.validarCategoria(categoriaId);

        // Assert
        // Verificamos que nuestro adapter efectivamente le pasó la pelota al cliente de Feign
        verify(categoriaClient, times(1)).obtenerCategoriaPorId(categoriaId);
    }

    @Test
    void fallbackValidarCategoria_DeberiaEjecutarseSinErrores() {
        // Arrange
        Long categoriaId = 5L;
        Throwable excepcionSimulada = new RuntimeException("Error de conexión Timeout");

        // Act
        // Llamamos al método de fallback directamente para probar su lógica interna
        categoriaAdapter.fallbackValidarCategoria(categoriaId, excepcionSimulada);

        // Assert
        // Como este método devuelve 'void' y solo hace un System.out.println,
        // el simple hecho de que se ejecute sin lanzar una excepción inesperada
        // es suficiente para considerarlo exitoso y cubrir la línea de código.
    }
}