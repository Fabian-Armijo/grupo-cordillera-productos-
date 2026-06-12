package com.cordillera.productos.controller;

import com.cordillera.productos.dto.ProductoRequestDTO;
import com.cordillera.productos.dto.ProductoResponseDTO;
import com.cordillera.productos.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // 🔒 CREAR PRODUCTO BLINDADO MULTI-SUCURSAL (Hereda la sucursal del token por defecto)
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(
            @RequestHeader(value = "X-User-Role", required = false) String rol,
            @RequestHeader(value = "X-Sucursal-Id", required = false) Long sucursalIdDelToken,
            @Valid @RequestBody ProductoRequestDTO request) {

        ProductoResponseDTO nuevoProducto = productoService.crearProducto(request, rol, sucursalIdDelToken);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    // 🛡️ LISTAR PRODUCTOS CON AISLAMIENTO DE DATOS
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listarTodos(
            @RequestHeader(value = "X-User-Role", required = false) String rol,
            @RequestHeader(value = "X-Sucursal-Id", required = false) Long sucursalId) {

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
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
            @RequestHeader(value = "X-User-Role", required = false) String rol,
            @RequestHeader(value = "X-Sucursal-Id", required = false) Long sucursalIdDelToken,
            @PathVariable Long id) {

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
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @RequestHeader(value = "X-User-Role", required = false) String rol,
            @RequestHeader(value = "X-Sucursal-Id", required = false) Long sucursalIdDelToken,
            @PathVariable Long id) {

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
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<java.util.Map<String, String>> manejarValidaciones(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
    }
}