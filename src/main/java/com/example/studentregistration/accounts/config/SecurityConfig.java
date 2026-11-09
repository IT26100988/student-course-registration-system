package com.example.studentregistration.accounts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security config for the student project.
 * - Public: register, login, csrf
 * - ADMIN only: /api/v1/admin/**
 * - Authenticated: everything else under /api/v1
 * - CSRF enabled (cookie token via /api/v1/auth/csrf)
 * - Stateless? No — we use session cookie (JSESSIONID).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login"
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        // Static pages + assets (frontend)
                        .requestMatchers(
                                "/", "/index.html",
                                "/student/**", "/admin/**",
                                "/css/**", "/js/**", "/favicon.ico",
                                "/*.html", "/error"
                        ).permitAll()
                        // Public auth endpoints
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/csrf"
                        ).permitAll()
                        // Admin-only
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // Student-only (own data)
                        .requestMatchers("/api/v1/students/me/**").hasRole("STUDENT")
                        // Anything else under /api/v1 needs authentication
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll()
                )
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .formLogin(form -> form.disable())     // no HTML login page
                .httpBasic(basic -> basic.disable())   // no Basic auth
                .logout(logout -> logout.disable());   // we handle logout in controller

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:8081"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}
