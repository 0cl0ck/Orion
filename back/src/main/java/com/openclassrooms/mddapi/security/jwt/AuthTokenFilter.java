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

/**
 * Filtre d'authentification JWT qui intercepte chaque requête HTTP pour vérifier 
 * la présence et la validité d'un token JWT.
 * 
 * Ce filtre s'exécute une fois par requête (OncePerRequestFilter) et vérifie si la requête 
 * contient un token JWT valide dans l'en-tête Authorization. Si c'est le cas, il extrait 
 * l'identifiant utilisateur du token, charge les détails de l'utilisateur et configure 
 * le contexte de sécurité Spring Security.
 * 
 * Le filtre utilise JwtUtils pour valider le token et extraire les informations utilisateur.
 */
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    /**
     * Méthode principale du filtre qui traite chaque requête HTTP.
     * Extrait et valide le token JWT, puis configure le contexte de sécurité si le token est valide.
     * 
     * @param request La requête HTTP entrante
     * @param response La réponse HTTP sortante
     * @param filterChain La chaîne de filtres pour continuer le traitement
     * @throws ServletException En cas d'erreur liée au servlet
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String jwt = parseJwt(request);
            
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // Récupérer l'ID de l'utilisateur à partir du token
                Long userId = jwtUtils.getUserIdFromJwtToken(jwt);
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                
                // Log minimal pour le débogage
                if (logger.isDebugEnabled()) {
                    logger.debug("JWT valide pour l'utilisateur: {}", username);
                }
                
                // Charger l'utilisateur par ID
                UserDetails userDetails = userDetailsService.loadUserById(userId);
                
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Erreur d'authentification: {}", e.getMessage());
            // Pour le débogage, on peut activer le stack trace complet si nécessaire
            if (logger.isDebugEnabled()) {
                logger.debug("Détails de l'exception:", e);
            }
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Extrait le token JWT de l'en-tête Authorization de la requête HTTP.
     * Le format attendu est "Bearer [token]"
     * 
     * @param request La requête HTTP
     * @return Le token JWT extrait, ou null si aucun token n'est présent ou valide
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
} 