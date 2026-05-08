package com.cordillera.productos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients // Habilita la comunicación con otros microservicios
public class ProductosApplication {
	public static void main(String[] args) {
		SpringApplication.run(ProductosApplication.class, args);
	}
}
