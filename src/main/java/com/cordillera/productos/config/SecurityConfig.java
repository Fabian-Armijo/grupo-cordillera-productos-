package com.cordillera.productos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitamos CSRF porque es una API REST (no usamos cookies/formularios web)
                .csrf(csrf -> csrf.disable())

                // 2. Configuramos los permisos de las rutas
                .authorizeHttpRequests(auth -> auth
                        // Permitir peticiones GET a todos (público)
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
                        // Cualquier otra petición (POST, DELETE, PUT) requiere autenticación
                        .anyRequest().authenticated()
                )

                // 3. Usamos Autenticación Básica (usuario y contraseña en el Header)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // Configuración de un usuario en memoria para hacer pruebas rápidas
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("postgres")
                .password("{noop}abc123") // {noop} indica que la contraseña no está encriptada (solo para pruebas)
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}
