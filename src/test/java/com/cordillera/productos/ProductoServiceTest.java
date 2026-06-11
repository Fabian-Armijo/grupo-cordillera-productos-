package com.cordillera.productos;

import com.cordillera.productos.dto.ProductoRequestDTO;
import com.cordillera.productos.dto.ProductoResponseDTO;
import com.cordillera.productos.model.Producto;
import com.cordillera.productos.repository.ProductoRepository;
import com.cordillera.productos.service.CategoriaClientAdapter;
import com.cordillera.productos.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Usamos Mockito puro, sin cargar Spring Boot
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository; // Simulamos la base de datos

    @Mock
    private CategoriaClientAdapter categoriaAdapter; // Simulamos la conexión con el otro microservicio

    @InjectMocks
    private ProductoService productoService; // La clase real que estamos probando

    private ProductoRequestDTO requestDTO;
    private Producto producto;

    @BeforeEach
    void setUp() {
        // Preparamos los datos de entrada
        requestDTO = new ProductoRequestDTO();
        requestDTO.setSku("TEC-001");
        requestDTO.setNombre("Teclado Mecánico RGB");
        requestDTO.setPrecio(45000.0);
        requestDTO.setCosto(25000.0);
        requestDTO.setCategoriaId(2L);
        // No seteamos el 'activo' para probar que por defecto se asigna 'true'

        // Preparamos la entidad simulada que devolverá el repositorio
        producto = new Producto();
        producto.setId(1L);
        producto.setSku("TEC-001");
        producto.setNombre("Teclado Mecánico RGB");
        producto.setPrecio(45000.0);
        producto.setCosto(25000.0);
        producto.setCategoriaId(2L);
        producto.setActivo(true);
    }

    @Test
    void crearProducto_DeberiaCrearYRetornarResponseDTO() {
        // Arrange (Preparación)
        // 1. Simulamos que el validador de categoría no hace nada (pasa exitosamente)
        doNothing().when(categoriaAdapter).validarCategoria(requestDTO.getCategoriaId());
        // 2. Simulamos que al guardar en base de datos, retorna nuestro producto con ID
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // Act (Ejecución)
        ProductoResponseDTO resultado = productoService.crearProducto(requestDTO);

        // Assert (Validación)
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("TEC-001", resultado.getSku());
        assertTrue(resultado.getActivo()); // Validamos que el null se convirtió en true por defecto

        // Verificamos que los mocks fueron llamados exactamente 1 vez
        verify(categoriaAdapter, times(1)).validarCategoria(requestDTO.getCategoriaId());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void obtenerTodos_DeberiaRetornarListaDeResponseDTO() {
        // Arrange
        when(productoRepository.findAll()).thenReturn(Arrays.asList(producto));

        // Act
        List<ProductoResponseDTO> resultado = productoService.obtenerTodos();

        // Assert
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals("Teclado Mecánico RGB", resultado.get(0).getNombre());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorId_CuandoExiste_DeberiaRetornarResponseDTO() {
        // Arrange
        Long idBuscado = 1L;
        when(productoRepository.findById(idBuscado)).thenReturn(Optional.of(producto));

        // Act
        ProductoResponseDTO resultado = productoService.obtenerPorId(idBuscado);

        // Assert
        assertNotNull(resultado);
        assertEquals(idBuscado, resultado.getId());
        verify(productoRepository, times(1)).findById(idBuscado);
    }

    @Test
    void obtenerPorId_CuandoNoExiste_DeberiaLanzarExcepcion() {
        // Arrange
        Long idBuscado = 99L;
        when(productoRepository.findById(idBuscado)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            productoService.obtenerPorId(idBuscado);
        });

        assertEquals("Producto no encontrado con ID: " + idBuscado, excepcion.getMessage());
        verify(productoRepository, times(1)).findById(idBuscado);
    }

    @Test
    void eliminarProducto_CuandoExiste_DeberiaEliminar() {
        // Arrange
        Long idAEliminar = 1L;
        when(productoRepository.existsById(idAEliminar)).thenReturn(true);
        doNothing().when(productoRepository).deleteById(idAEliminar);

        // Act
        productoService.eliminarProducto(idAEliminar);

        // Assert
        verify(productoRepository, times(1)).existsById(idAEliminar);
        verify(productoRepository, times(1)).deleteById(idAEliminar);
    }

    @Test
    void eliminarProducto_CuandoNoExiste_DeberiaLanzarExcepcion() {
        // Arrange
        Long idAEliminar = 99L;
        when(productoRepository.existsById(idAEliminar)).thenReturn(false);

        // Act & Assert
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            productoService.eliminarProducto(idAEliminar);
        });

        assertEquals("No se puede eliminar: Producto no encontrado", excepcion.getMessage());

        // Verificamos que la búsqueda se hizo, pero el borrado NUNCA se ejecutó
        verify(productoRepository, times(1)).existsById(idAEliminar);
        verify(productoRepository, never()).deleteById(anyLong());
    }
}