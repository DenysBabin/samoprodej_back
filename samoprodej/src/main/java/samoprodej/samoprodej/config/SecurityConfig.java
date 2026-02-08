package samoprodej.samoprodej.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Отключаем CSRF для API (для разработки)
            .authorizeHttpRequests(auth -> auth
                // Разрешаем все API эндпоинты без авторизации (для разработки)
                // Это включает GET /api/listings и GET /api/listings/{id}
                .requestMatchers("/api/**").permitAll()
                // Все остальные запросы требуют аутентификацию
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> {}); // Оставляем базовую HTTP аутентификацию для остальных эндпоинтов

        return http.build();
    }
}
