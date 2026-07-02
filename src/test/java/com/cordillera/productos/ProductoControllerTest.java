package com.cordillera.productos;

import com.cordillera.productos.controller.ProductoController;
import com.cordillera.productos.dto.ProductoRequestDTO;
import com.cordillera.productos.dto.ProductoResponseDTO;
import com.cordillera.productos.service.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import org.springframework.security.test.context.support.WithMockUser;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WithMockUser(username = "testUser", roles = {"ADMIN"})
@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductoRequestDTO requestDTO;
    private ProductoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        // 1. Preparamos el Request con datos que pasen TODAS las validaciones
        requestDTO = new ProductoRequestDTO();
        requestDTO.setSku("TEC-001");
        requestDTO.setNombre("Teclado Mecánico RGB");
        requestDTO.setDescripcion("Teclado para gaming con switches azules"); // Opcional
        requestDTO.setPrecio(45000.0);
        requestDTO.setCosto(25000.0);
        requestDTO.setCategoriaId(2L);
        requestDTO.setActivo(true); // Opcional

        // 2. Preparamos el Response esperado
        responseDTO = new ProductoResponseDTO();
        // Aquí deberías setear los campos de tu ProductoResponseDTO
        // Por ejemplo:
        // responseDTO.setId(1L);
        // responseDTO.setSku("TEC-001");
        // responseDTO.setNombre("Teclado Mecánico RGB");
    }

    @Test
    void crear_DeberiaRetornarProductoYStatusCreated() throws Exception {
        // Arrange (Preparación)
        when(productoService.crearProducto(any(ProductoRequestDTO.class))).thenReturn(responseDTO);

        // Act & Assert (Ejecución y Validación)
        mockMvc.perform(post("/api/productos")
                        .with(csrf()) // <-- AQUÍ INYECTAMOS EL TOKEN CSRF
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void listarTodos_DeberiaRetornarListaYStatusOK() throws Exception {
        // Arrange
        List<ProductoResponseDTO> listaProductos = Arrays.asList(responseDTO);
        when(productoService.obtenerTodos()).thenReturn(listaProductos);

        // Act & Assert
        mockMvc.perform(get("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void obtenerPorId_DeberiaRetornarProductoYStatusOK() throws Exception {
        // Arrange
        Long productoId = 1L;
        when(productoService.obtenerPorId(productoId)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/productos/{id}", productoId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void eliminar_DeberiaRetornarStatusNoContent() throws Exception {
        // Arrange
        Long productoId = 1L;
        doNothing().when(productoService).eliminarProducto(productoId);

        // Act & Assert
        mockMvc.perform(delete("/api/productos/{id}", productoId)
                        .with(csrf())) // <-- AQUÍ TAMBIÉN
                .andExpect(status().isNoContent());
    }
}