package com.cordillera.productos.controller;

import com.cordillera.productos.dto.ProductoRequestDTO;
import com.cordillera.productos.dto.ProductoResponseDTO;
import com.cordillera.productos.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Endpoints para la gestión del catálogo de productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Operation(summary = "Crear un nuevo producto", description = "Guarda un producto en la base de datos tras validar su categoría")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Role", required = false) String rol,
            @Parameter(hidden = true) @RequestHeader(value = "X-Sucursal-Id", required = false) Long sucursalIdDelToken,
            @Parameter(description = "Datos del producto a crear (nombre, precio, sku, etc.)") @Valid @RequestBody ProductoRequestDTO request) {

        ProductoResponseDTO nuevoProducto = productoService.crearProducto(request, rol, sucursalIdDelToken);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    @Operation(summary = "Listar todos los productos", description = "Retorna una lista completa de todos los productos en el inventario")
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listarTodos(
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Role", required = false) String rol,
            @Parameter(hidden = true) @RequestHeader(value = "X-Sucursal-Id", required = false) Long sucursalId) {

        System.out.println("[MS-PRODUCTOS] -> Interceptado Header X-User-Role: " + rol);
        System.out.println("[MS-PRODUCTOS] -> Interceptado Header X-Sucursal-Id: " + sucursalId);

        if (rol != null && !"ADMIN".equalsIgnoreCase(rol.trim()) && sucursalId != null) {
            System.out.println("[🔒 FILTRO INVENTARIO ACTIVO] -> Segregando catálogo para Sucursal ID: " + sucursalId);
            return ResponseEntity.ok(productoService.listarPorSucursal(sucursalId));
        }

        System.out.println("[🌐 INVENTARIO GLOBAL] -> Acceso de administrador. Desplegando maestro completo.");
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    @Operation(summary = "Obtener un producto por su ID", description = "Busca un producto específico utilizando su identificador único")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Role", required = false) String rol,
            @Parameter(hidden = true) @RequestHeader(value = "X-Sucursal-Id", required = false) Long sucursalIdDelToken,
            @Parameter(description = "ID único del producto en la base de datos") @PathVariable Long id) {

        ProductoResponseDTO producto = productoService.obtenerPorId(id);

        // Bloquear lectura cruzada si el usuario no es ADMIN y el producto pertenece a otra sucursal
        if (rol != null && !"ADMIN".equalsIgnoreCase(rol.trim()) && sucursalIdDelToken != null) {
            if (!sucursalIdDelToken.equals(producto.getSucursalId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(java.util.Map.of("message", "Acceso denegado: El recurso solicitado no pertenece a tu sucursal."));
            }
        }

        return ResponseEntity.ok(producto);
    }

    @Operation(summary = "Eliminar un producto", description = "Elimina físicamente un producto de la base de datos a partir de su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado para eliminar")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Role", required = false) String rol,
            @Parameter(hidden = true) @RequestHeader(value = "X-Sucursal-Id", required = false) Long sucursalIdDelToken,
            @Parameter(description = "ID del producto a dar de baja") @PathVariable Long id) {

        // Buscamos el producto primero para validar a quién le pertenece antes de borrar
        ProductoResponseDTO producto = productoService.obtenerPorId(id);

        // Si no es ADMIN y el producto es de otra sucursal, rechazamos el borrado
        if (rol != null && !"ADMIN".equalsIgnoreCase(rol.trim()) && sucursalIdDelToken != null) {
            if (!sucursalIdDelToken.equals(producto.getSucursalId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(java.util.Map.of("message", "Acceso denegado: No tienes permisos para eliminar recursos de otra sucursal."));
            }
        }

        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}