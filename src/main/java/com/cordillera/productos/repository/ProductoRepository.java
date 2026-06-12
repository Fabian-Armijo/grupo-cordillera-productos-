package com.cordillera.productos.repository;

import com.cordillera.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Método personalizado para buscar por sku (ya que es único)
    Optional<Producto> findBySku(String sku);

    // Metodo para buscar productos de una categoría específica
    List<Producto> findByCategoriaId(Long categoriaId);

    // 🛡️ SOLUCIÓN PARA REGISTROS CRUZADOS: Filtro estricto por Sucursal
    List<Producto> findBySucursalId(Long sucursalId);
}