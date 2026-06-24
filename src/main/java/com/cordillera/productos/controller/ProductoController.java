package com.cordillera.productos.controller;

import com.cordillera.productos.dto.ProductoRequestDTO;
import com.cordillera.productos.dto.ProductoResponseDTO;
import com.cordillera.productos.service.ProductoService;

// Importaciones de Swagger / OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Catálogo de Productos", description = "Endpoints para la gestión del inventario y catálogo de productos con soporte de aislamiento multi-sucursal")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // 🔒 CREAR PRODUCTO BLINDADO MULTI-SUCURSAL (Hereda la sucursal del token por defecto)
    @Operation(
            summary = "Crear nuevo producto",
            description = "Registra un nuevo producto en el catálogo. Si el usuario no es ADMIN, el producto se asocia automáticamente a la sucursal del usuario que realiza la petición."
    )
    @ApiResponse(responseCode = "201", description = "Producto creado y guardado exitosamente")
    @ApiResponse(responseCode = "400", description = "Error de validación en los datos enviados")
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Role", required = false) String rol,
            @Parameter(hidden = true) @RequestHeader(value = "X-Sucursal-Id", required = false) Long sucursalIdDelToken,
            @Parameter(description = "Datos del producto a crear (nombre, precio, sku, etc.)") @Valid @RequestBody ProductoRequestDTO request) {

        ProductoResponseDTO nuevoProducto = productoService.crearProducto(request, rol, sucursalIdDelToken);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    // 🛡️ LISTAR PRODUCTOS CON AISLAMIENTO DE DATOS
    @Operation(
            summary = "Listar catálogo de productos",
            description = "Retorna la lista de productos. Los administradores ven el inventario global, mientras que los gerentes solo ven el catálogo segregado de su propia sucursal."
    )
    @ApiResponse(responseCode = "200", description = "Catálogo recuperado con éxito")
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

    // 🛡️ OBTENER UN PRODUCTO POR ID CON CANDADO DE SEGURIDAD
    @Operation(
            summary = "Obtener producto por ID",
            description = "Busca el detalle de un producto específico. Protegido contra lecturas cruzadas: un usuario no puede leer productos de una sucursal distinta a la suya."
    )
    @ApiResponse(responseCode = "200", description = "Producto encontrado y devuelto")
    @ApiResponse(responseCode = "403", description = "Acceso denegado (el producto pertenece a otra sucursal)")
    @ApiResponse(responseCode = "404", description = "El producto no existe")
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

    // 🛡️ ELIMINAR CON CANDADO DE SEGURIDAD
    @Operation(
            summary = "Eliminar producto",
            description = "Borra un producto del sistema. Verifica estrictamente los permisos antes de ejecutar la eliminación."
    )
    @ApiResponse(responseCode = "204", description = "Producto eliminado con éxito (Sin contenido de respuesta)")
    @ApiResponse(responseCode = "403", description = "Acceso denegado (Intento de eliminación en otra sucursal)")
    @ApiResponse(responseCode = "404", description = "El producto no existe")
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

    // 🧼 MANEJADOR DE EXCEPCIONES PARA AGREGAR ROBUSTEZ A LA API
    @Operation(hidden = true) // Ocultamos el manejador de errores de la interfaz gráfica de Swagger
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<java.util.Map<String, String>> manejarValidaciones(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
    }
}