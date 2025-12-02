// aqm-back-end/src/main/java/.../config/SecurityConfig.java (FIXED)
package com.commander.aqm.aqm_back_end.config;

import com.commander.aqm.aqm_back_end.security.JwtAuthFilter;
import com.commander.aqm.aqm_back_end.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ Allow frontend origins
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://localhost:8080"
        ));

        // ✅ Allow all HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // ✅ Allow all headers
        config.setAllowedHeaders(List.of("*"));

        // ✅ Allow credentials (for cookies/auth headers)
        config.setAllowCredentials(true);

        // ✅ Cache preflight requests for 1 hour
        config.setMaxAge(3600L);

        // ✅ Expose Authorization header to frontend
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ✅ Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ✅ DISABLE CSRF (important for REST APIs)
                .csrf(AbstractHttpConfigurer::disable)

                // ✅ Stateless session (JWT-based, no server session)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ✅ Configure endpoint permissions
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - NO AUTH REQUIRED
                        .requestMatchers(
                                "/api/auth/**",           // Login, Register
                                "/api/health",            // Health check
                                "/swagger-ui/**",         // Swagger UI
                                "/v3/api-docs/**",        // API docs
                                "/swagger-ui.html"        // Swagger HTML
                        ).permitAll()

                        // Admin endpoints - REQUIRE ADMIN ROLE
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ✅ NEW: Profile endpoints - authenticated users
                        .requestMatchers("/api/user/profile/**").authenticated()
                        .requestMatchers("/api/user/test-email").authenticated()

                        // All other endpoints - REQUIRE AUTHENTICATION
                        .anyRequest().authenticated()
                )

                // ✅ Add JWT filter BEFORE UsernamePasswordAuthenticationFilter
                .addFilterBefore(
                        new JwtAuthFilter(jwtUtils, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}