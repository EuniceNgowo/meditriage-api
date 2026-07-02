package com.example.triage_api.config;

import com.example.triage_api.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            /*
             * Disable CSRF protection.
             * REST APIs with stateless JWT authentication do not need CSRF tokens.
             */
            .csrf(AbstractHttpConfigurer::disable)

            /*
             * CORS configuration — allows the Flutter app to make API calls.
             * See corsConfigurationSource() bean below for the allowed origins.
             */
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            /*
             * Session management — set to STATELESS.
             * Spring Security will never create an HTTP session.
             * Each request must authenticate independently via JWT.
             */
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            /*
             * Authorization rules — who can access what:
             *   permitAll()     — no authentication required (public endpoints)
             *   authenticated() — must have a valid JWT token
             *
             * Order matters: more specific matchers must come before broader ones.
             */
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no token required
                .requestMatchers(HttpMethod.GET,
                    "/api/doctors",         // browse all doctors (public)
                    "/api/doctors/available", // browse available doctors
                    "/api/doctors/{id}"     // view a specific doctor
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                    "/api/doctors/register",        // doctor self-registration (public)
                    "/api/doctors/login",            // doctor login (public)
                    "/api/doctors/register-phone",   // doctor phone registration (public)
                    "/api/doctors/login-phone"       // doctor phone login (public)
                ).permitAll()
                .requestMatchers(
                    "/api/auth/**",         // register, login, doctor login
                    "/swagger-ui/**",       // Swagger UI pages
                    "/swagger-ui.html",     // Swagger main page
                    "/api-docs/**",         // OpenAPI JSON specification
                    "/v3/api-docs/**"       // OpenAPI alternate path
                ).permitAll()
                // All other endpoints require a valid JWT token
                .anyRequest().authenticated()
            )

            /*
             * Set up our custom DaoAuthenticationProvider so Spring Security
             * knows to use our UserDetailsServiceImpl and BCryptPasswordEncoder.
             */
            .authenticationProvider(authenticationProvider())

            /*
             * Insert our JWT filter BEFORE Spring's built-in
             * UsernamePasswordAuthenticationFilter.
             * This ensures the JWT is validated before Spring attempts
             * its default form-login authentication.
             */
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));

        // Allow standard HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow the headers our API uses
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        // Allow browsers to read the Authorization header from responses
        configuration.setExposedHeaders(List.of("Authorization"));

        // Cache the pre-flight OPTIONS response for 1 hour (reduces network overhead)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
