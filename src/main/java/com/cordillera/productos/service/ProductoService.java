package com.cordillera.productos.service;

import com.cordillera.productos.Client.CategoriaClient;
import com.cordillera.productos.dto.ProductoRequestDTO;
import com.cordillera.productos.dto.ProductoResponseDTO;
import com.cordillera.productos.model.Producto;
import com.cordillera.productos.repository.ProductoRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaClient categoriaClient; //Inyección del cliente Feign

    @Autowired
    private CategoriaClientAdapter categoriaAdapter;

    //Crear un nuevo producto
    @Transactional
    public ProductoResponseDTO crearProducto(ProductoRequestDTO request) {

        categoriaAdapter.validarCategoria(request.getCategoriaId());

        //Procede a guardar si la validación fue exitosa o si entró al fallback permitido
        Producto producto = new Producto();
        producto.setSku(request.getSku());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setCosto(request.getCosto());
        producto.setCategoriaId(request.getCategoriaId());
        if (request.getActivo() != null) {
            producto.setActivo(request.getActivo());
        } else {
            producto.setActivo(true); //Si no lo envían, nace activo por defecto
        }

        Producto guardado = productoRepository.save(producto);
        return mapToResponseDTO(guardado);
    }

    //Obtener todos los productos
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerTodos() {
        List<Producto> productos = productoRepository.findAll();
        return productos.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    //Obtener por ID
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        return mapToResponseDTO(producto);
    }

    //Eliminar producto
    @Transactional
    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar: Producto no encontrado");
        }
        productoRepository.deleteById(id);
    }

    @CircuitBreaker(name = "categoriaCB", fallbackMethod = "fallbackValidarCategoria")
    public void validarCategoria(Long categoriaId) {
        log.info("Llamando al microservicio de Categorías para validar ID: {}", categoriaId);
        categoriaClient.obtenerCategoriaPorId(categoriaId);
    }

    // Metodo de contingencia (Fallback) si el microservicio de Categorías falla o está apagado
    public void fallbackValidarCategoria(Long categoriaId, Throwable excepcion) {
        log.warn("ADVERTENCIA: Falló la validación con el microservicio de Categorías. " +
                        "Razón: {}. Se permitirá guardar el producto asumiendo que el ID {} es válido.",
                excepcion.getMessage(), categoriaId);
    }


    // Metodo privado para convertir Entidad a DTO (Mapeo)
    private ProductoResponseDTO mapToResponseDTO(Producto producto) {
        return ProductoResponseDTO.builder()
                .id(producto.getId())
                .sku(producto.getSku())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .categoriaId(producto.getCategoriaId())
                .activo(producto.getActivo())
                .build();
    }
}