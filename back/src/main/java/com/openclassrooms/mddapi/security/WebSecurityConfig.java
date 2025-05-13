package com.openclassrooms.mddapi.security;

import com.openclassrooms.mddapi.security.jwt.AuthEntryPointJwt;
import com.openclassrooms.mddapi.security.jwt.AuthTokenFilter;
import com.openclassrooms.mddapi.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;

/**
 * Configuration principale de la sécurité de l'application.
 * 
 * Cette classe configure Spring Security pour l'application, notamment :
 * - L'authentification basée sur JWT
 * - Les règles d'autorisation pour les différentes URL
 * - La gestion des sessions (stateless)
 * - Les headers de sécurité HTTP
 * - L'accès aux endpoints Actuator
 * 
 * @EnableWebSecurity active la sécurité web de Spring Security
 * @EnableMethodSecurity permet l'utilisation des annotations de sécurité comme @PreAuthorize
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    /**
     * Crée et configure le filtre d'authentification JWT.
     * Ce filtre intercepte les requêtes pour vérifier la présence et la validité des tokens JWT.
     * 
     * @return Une instance configurée de AuthTokenFilter
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * Configure le fournisseur d'authentification qui vérifie les identifiants des utilisateurs.
     * Utilise le UserDetailsService pour charger les détails des utilisateurs et le PasswordEncoder
     * pour vérifier les mots de passe.
     * 
     * @return Le fournisseur d'authentification configuré
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        
        return authProvider;
    }

    /**
     * Expose le gestionnaire d'authentification de Spring Security.
     * Ce gestionnaire est utilisé pour authentifier les utilisateurs lors de la connexion.
     * 
     * @param authConfig La configuration d'authentification
     * @return Le gestionnaire d'authentification
     * @throws Exception Si une erreur se produit lors de la récupération du gestionnaire
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configure l'encodeur de mot de passe utilisé pour hacher les mots de passe des utilisateurs.
     * Utilise BCrypt, un algorithme de hachage sécurisé avec salage automatique.
     * 
     * @return L'encodeur de mot de passe configuré
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure la chaîne de filtres de sécurité principale de l'application.
     * Définit les règles d'accès aux différentes URL, la gestion des sessions,
     * les headers de sécurité HTTP, et les filtres d'authentification.
     * 
     * @param http L'objet de configuration de sécurité HTTP
     * @return La chaîne de filtres de sécurité configurée
     * @throws Exception Si une erreur se produit lors de la configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Ajout des headers de sécurité HTTP
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()) // X-Frame-Options: SAMEORIGIN
                .defaultsDisabled() // Désactiver les valeurs par défaut
                .cacheControl(cache -> {}) // Activer Cache-Control
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'")) // CSP
            )
            .authorizeHttpRequests(auth -> 
                auth.requestMatchers("/api/auth/login").permitAll()
                    .requestMatchers("/api/auth/register").permitAll()
                    .requestMatchers("/api/test/public").permitAll()
                    .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                    .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
                    // Autoriser l'accès à Swagger UI et OpenAPI sans authentification
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/swagger-ui.html").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/api-docs/**").permitAll()
                    .anyRequest().authenticated()
            );
        
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}