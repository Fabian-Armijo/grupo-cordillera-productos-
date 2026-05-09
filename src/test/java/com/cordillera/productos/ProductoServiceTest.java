package com.cordillera.productos;

import com.cordillera.productos.dto.ProductoRequestDTO;
import com.cordillera.productos.dto.ProductoResponseDTO;
import com.cordillera.productos.model.Producto;
import com.cordillera.productos.repository.ProductoRepository;
import com.cordillera.productos.service.CategoriaClientAdapter;
import com.cordillera.productos.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    // 1. Mocks (Dependencias simuladas)
    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaClientAdapter categoriaAdapter;

    // 2. Clase a probar con los Mocks inyectados
    @InjectMocks
    private ProductoService productoService;

    // ====================================================================
    // PRUEBA 1: Creación Exitosa (El "Camino Feliz")
    // ====================================================================
    @Test
    void crearProducto_DeberiaGuardarYRetornarProducto() {
        // ARRANGE (Preparar)
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("LAP-123");
        request.setNombre("Laptop Test");
        request.setCategoriaId(1L);
        request.setActivo(true);

        Producto productoSimulado = new Producto();
        productoSimulado.setId(10L);
        productoSimulado.setSku("LAP-123");
        productoSimulado.setNombre("Laptop Test");
        productoSimulado.setCategoriaId(1L);
        productoSimulado.setActivo(true);

        when(productoRepository.save(any(Producto.class))).thenReturn(productoSimulado);
        doNothing().when(categoriaAdapter).validarCategoria(anyLong()); // Opcional, pero buena práctica explicitarlo

        // ACT (Ejecutar)
        ProductoResponseDTO response = productoService.crearProducto(request);

        // ASSERT (Verificar)
        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("LAP-123", response.getSku());
        assertTrue(response.getActivo());

        verify(categoriaAdapter, times(1)).validarCategoria(1L);
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    // ====================================================================
    // PRUEBA 2: Colisión de Datos (El usuario envía un SKU que ya existe)
    // ====================================================================
    @Test
    void crearProducto_DeberiaLanzarExcepcion_CuandoSkuYaExiste() {
        // ARRANGE
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setSku("LAP-123");
        request.setCategoriaId(1L);

        when(productoRepository.save(any(Producto.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint violation (SKU repetido)"));

        // ACT & ASSERT
        assertThrows(DataIntegrityViolationException.class, () -> {
            productoService.crearProducto(request);
        });

        verify(categoriaAdapter, times(1)).validarCategoria(1L);
    }

    // ====================================================================
    // PRUEBA 3: Búsqueda Exitosa
    // ====================================================================
    @Test
    void obtenerPorId_DeberiaRetornarProducto_CuandoExiste() {
        // ARRANGE
        Producto productoEnBaseDeDatos = new Producto();
        productoEnBaseDeDatos.setId(1L);
        productoEnBaseDeDatos.setNombre("Monitor 4K");
        productoEnBaseDeDatos.setActivo(true);

        // Simulamos el comportamiento del Optional que devuelve Spring Data JPA
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEnBaseDeDatos));

        // ACT
        ProductoResponseDTO response = productoService.obtenerPorId(1L);

        // ASSERT
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Monitor 4K", response.getNombre());

        verify(productoRepository, times(1)).findById(1L);
    }

    // ====================================================================
    // PRUEBA 4: Error 404 Lógico (Buscar un producto que no existe)
    // ====================================================================
    @Test
    void obtenerPorId_DeberiaLanzarExcepcion_CuandoNoExiste() {
        // ARRANGE
        // Simulamos que la base de datos devuelve un Optional vacío
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        // Si tienes una excepción personalizada (ej. ResourceNotFoundException), reemplaza RuntimeException
        Exception excepcion = assertThrows(RuntimeException.class, () -> {
            productoService.obtenerPorId(99L);
        });

        // Verificamos que al menos intente buscarlo
        verify(productoRepository, times(1)).findById(99L);
    }
}
