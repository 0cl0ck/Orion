package com.openclassrooms.mddapi.security.jwt;

import com.openclassrooms.mddapi.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Ajout de logs détaillés pour le debugging
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        logger.info("========== DÉBUT DE TRAITEMENT DE REQUÊTE ==========");
        logger.info("Requête reçue: {} {}", method, requestURI);
        
        // Log des en-têtes
        logger.info("En-têtes de la requête:");
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            logger.info("  {}: {}", headerName, request.getHeader(headerName));
        }
        
        try {
            String jwt = parseJwt(request);
            
            // Log le token extrait
            if (jwt != null) {
                logger.info("JWT extrait de l'en-tête Authorization: {}...", jwt.substring(0, Math.min(20, jwt.length())));
            } else {
                logger.warn("Aucun JWT trouvé dans la requête");
            }
            
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // Récupérer l'ID de l'utilisateur à partir du token (maintenant stocké dans le sujet)
                Long userId = jwtUtils.getUserIdFromJwtToken(jwt);
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                logger.info("JWT valide pour l'utilisateur ID: {} (username: {})", userId, username);
                
                // Charger l'utilisateur par ID plutôt que par nom d'utilisateur
                UserDetails userDetails = userDetailsService.loadUserById(userId);
                logger.info("Utilisateur chargé: {}, Rôles: {}", userDetails.getUsername(), userDetails.getAuthorities());
                
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("Authentification définie dans le contexte de sécurité");
            } else if (jwt != null) {
                logger.error("JWT présent mais invalide!");
            }
        } catch (Exception e) {
            logger.error("Impossible de définir l'authentification utilisateur pour {} {}: {}", method, requestURI, e.getMessage());
            logger.error("Exception complète:", e);
        }
        
        // Log avant de passer la requête au filtre suivant
        logger.info("État d'authentification avant de continuer: {}", 
                    (SecurityContextHolder.getContext().getAuthentication() != null ? 
                    "Authentifié (" + SecurityContextHolder.getContext().getAuthentication().getName() + ")" : 
                    "Non authentifié"));
        logger.info("Passage au filtre suivant");
        logger.info("========== FIN DE LOGS PRÉLIMINAIRES ==========");
        
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
} 