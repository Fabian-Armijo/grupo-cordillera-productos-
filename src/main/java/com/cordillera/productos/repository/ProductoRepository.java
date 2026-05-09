package com.cordillera.productos.repository;

import com.cordillera.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    //Metodo personalizado para buscar por sku (ya que es único)
    Optional<Producto> findBySku(String sku);

    // Metodo para buscar productos de una categoría específica
    java.util.List<Producto> findByCategoriaId(Long categoriaId);
}