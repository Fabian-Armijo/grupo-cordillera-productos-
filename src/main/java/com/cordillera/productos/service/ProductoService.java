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
    private CategoriaClient categoriaClient; // Inyección del cliente Feign

    @Autowired
    private CategoriaClientAdapter categoriaAdapter;

    // 🔒 CREAR PRODUCTO CON HERENCIA AUTOMÁTICA DE SUCURSAL DESDE EL TOKEN
    @Transactional
    public ProductoResponseDTO crearProducto(ProductoRequestDTO request, String rolDelToken, Long sucursalIdDelToken) {

        categoriaAdapter.validarCategoria(request.getCategoriaId());

        Producto producto = new Producto();
        producto.setSku(request.getSku());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setCosto(request.getCosto());
        producto.setCategoriaId(request.getCategoriaId());

        // 🛡️ POLÍTICA DE SEGURIDAD MULTI-SUCURSAL EN MUTACIONES
        if (rolDelToken != null && "ADMIN".equalsIgnoreCase(rolDelToken.trim())) {
            // El Administrador Corporativo central SÍ tiene permitido indicar la sucursal manualmente
            if (request.getSucursalId() == null) {
                throw new IllegalArgumentException("Un usuario administrador debe especificar explícitamente el ID de la sucursal.");
            }
            producto.setSucursalId(request.getSucursalId());
            log.info("[MS-PRODUCTOS] ADMIN creó producto asignado manualmente a sucursalId: {}", request.getSucursalId());
        } else {
            // Si es GERENTE o USUARIO, se ignora por completo el JSON del cliente
            // y se estampa obligatoriamente la sucursal de su token de sesión.
            if (sucursalIdDelToken == null) {
                throw new IllegalArgumentException("Operación Denegada: El token de usuario no cuenta con un identificador de sucursal válido.");
            }
            producto.setSucursalId(sucursalIdDelToken);
            log.info("[MS-PRODUCTOS] Usuario con rol {} creó producto. Sucursal asignada por contexto: {}", rolDelToken, sucursalIdDelToken);
        }

        if (request.getActivo() != null) {
            producto.setActivo(request.getActivo());
        } else {
            producto.setActivo(true); // Si no lo envían, nace activo por defecto
        }

        Producto guardado = productoRepository.save(producto);
        return mapToResponseDTO(guardado);
    }

    // Obtener todos los productos (Acceso global para ADMIN corporativo)
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerTodos() {
        List<Producto> productos = productoRepository.findAll();
        return productos.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // 🛡️ Listar productos con aislamiento estricto por Sucursal
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarPorSucursal(Long sucursalId) {
        log.info("[MS-PRODUCTOS] Ejecutando query aislada para sucursalId: {}", sucursalId);
        List<Producto> productos = productoRepository.findBySucursalId(sucursalId);
        return productos.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Obtener por ID
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        return mapToResponseDTO(producto);
    }

    // Eliminar producto
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

    // Metodo privado para convertir Entidad a DTO (Mapeo) asd
    private ProductoResponseDTO mapToResponseDTO(Producto producto) {
        return ProductoResponseDTO.builder()
                .id(producto.getId())
                .sku(producto.getSku())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .categoriaId(producto.getCategoriaId())
                .sucursalId(producto.getSucursalId()) // 🏢 Mapeo seguro de la columna recién creada hacia el DTO
                .activo(producto.getActivo())
                .build();
    }
}