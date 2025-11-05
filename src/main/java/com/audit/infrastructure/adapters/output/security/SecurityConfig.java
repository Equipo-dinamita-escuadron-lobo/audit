package com.audit.infrastructure.adapters.output.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;


import lombok.RequiredArgsConstructor;

/**
 * @brief Spring Security configuration for JWT-based authentication
 * 
 * Configures OAuth2 resource server with JWT authentication,
 * CSRF protection disabled, and stateless session management.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Profile("!test")
public class SecurityConfig {

    @Autowired
    private JwtAuthConverter jwtAuthConverter;

    /**
     * @brief Configures the security filter chain for HTTP requests
     * 
     *        Disables CSRF protection, permits anonymous access to Swagger and
     *        actuator endpoints,
     *        requires authentication for all other endpoints, and configures JWT
     *        authentication
     *        with stateless session management.
     *
     * @param httpSecurity The HttpSecurity object to configure
     * @return The configured security filter chain
     * @throws Exception If there's an error configuring the security filter chain
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(http -> http
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(oauth -> {
                    oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter));
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

}
