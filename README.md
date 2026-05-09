# Microservicio de Productos - Ecosistema Cordillera

Este proyecto es el **Servicio Core (Core Service)** encargado de gestionar el catálogo de productos de la arquitectura. Su responsabilidad es mantener la fuente de la verdad sobre los artículos, sus precios, costos, descripciones y su relación lógica con el catálogo de categorías.

A diferencia del BFF o el API Gateway, este microservicio es autónomo y posee su propia base de datos relacional para garantizar el aislamiento de datos (Database-per-service pattern).

## 🚀 Características Principales

*   **Gestión de Catálogo:** Operaciones CRUD (Crear, Leer, Actualizar, Eliminar/Desactivar) para la entidad `Producto`.
*   **Aislamiento de Datos:** Conexión exclusiva a su propia base de datos PostgreSQL.
*   **Auto-Poblado de Datos:** Configurado para inyectar datos de prueba (`data.sql`) automáticamente en entornos de desarrollo mediante la configuración `spring.sql.init.mode=always`.
*   **Capa de Servicio Robusta:** Implementación del patrón Repository para una interacción limpia y segura con la base de datos.

## 📋 Requisitos Previos

Asegúrate de tener instalado y en ejecución lo siguiente en tu entorno:

*   [Java Development Kit (JDK) 17 o superior](https://adoptium.net/)
*   [Apache Maven](https://maven.apache.org/) (o Wrapper `./mvnw`)
*   **PostgreSQL** (Servidor activo y base de datos creada para este microservicio)

## ⚙️ Configuración de Base de Datos

Antes de ejecutar el proyecto, verifica que el archivo `src/main/resources/application.properties` (o `.yml`) apunte correctamente a tu instancia de PostgreSQL local:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/tu_base_de_datos_productos
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña

# Configuración vital para el entorno de desarrollo
spring.jpa.hibernate.ddl-auto=update
spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always
```

🛠️ Cómo Ejecutar el Proyecto
Este microservicio suele ejecutarse en el puerto 8081 (revisar configuración local).

Opción 1: Usando el IDE (Desarrollo)
Abre el proyecto en tu IDE (IntelliJ, VS Code, Eclipse).

Localiza la clase ProductosApplication.java.

Haz clic en Run (▶). El sistema conectará a PostgreSQL, creará las tablas (si no existen) y cargará los datos de data.sql.

🧪 Cómo Probar el Microservicio
1. Pruebas Unitarias Automatizadas
El proyecto cuenta con una suite de pruebas unitarias construidas con JUnit 5 y Mockito. Estas pruebas validan la lógica de negocio (el ProductoService) sin tocar la base de datos real, simulando el ProductoRepository.

Para ejecutar toda la suite de pruebas y verificar que el código está sano (Exit code 0), ejecuta:

Bash
```properties
mvn test
```
2. Pruebas de Integración Manual (Postman / cURL)
Una vez que la aplicación esté corriendo, puedes probar los endpoints directamente (sin pasar por el API Gateway ni el BFF).

Consultar un producto existente (Cargado vía data.sql):

Método: GET

URL: http://localhost:8081/api/productos/10

Respuesta esperada: JSON con los datos del MacBook Pro 14.

Crear un nuevo producto:

Método: POST

URL: http://localhost:8081/api/productos

Headers: Content-Type: application/json

Body (raw JSON):

JSON
```properties
{
    "sku": "ACC-MOU-01",
    "nombre": "Mouse Inalámbrico",
    "descripcion": "Mouse óptico bluetooth",
    "precio": 25.99,
    "costo": 10.50,
    "categoriaId": 1,
    "activo": true
}
```

🛡️ Solución de Problemas Frecuentes
Error Connection to localhost:5432 refused: El microservicio no puede encontrar PostgreSQL. Asegúrate de que el servicio de base de datos esté encendido y las credenciales en el application.properties sean correctas.

Error DataIntegrityViolationException al arrancar: Suele ocurrir si el data.sql intenta insertar un producto con un sku que ya existe o viola una restricción nullable = false.

Cambios en Entidades no reflejados: Si agregas un nuevo atributo a Producto.java y falla, asegúrate de actualizar el archivo data.sql para incluir esa nueva columna en las sentencias INSERT.
