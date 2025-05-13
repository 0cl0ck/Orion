package com.openclassrooms.mddapi.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.mddapi.exceptions.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

/**
 * Point d'entrée pour gérer les erreurs d'authentification dans l'application.
 * 
 * Cette classe est appelée automatiquement par Spring Security lorsqu'une exception
 * d'authentification se produit, typiquement quand un utilisateur non authentifié
 * tente d'accéder à une ressource protégée.
 * 
 * Elle gère la création d'une réponse d'erreur formatée en JSON avec le code
 * d'état HTTP approprié (401 Unauthorized ou 404 Not Found dans certains cas spéciaux).
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Méthode appelée lorsqu'une exception d'authentification est levée.
     * Gère la création d'une réponse d'erreur appropriée.
     * 
     * Cette méthode détecte également les cas où une EntityNotFoundException
     * a été transformée en erreur d'authentification et renvoie alors
     * une réponse 404 Not Found au lieu d'une 401 Unauthorized.
     * 
     * @param request La requête HTTP qui a provoqué l'erreur
     * @param response La réponse HTTP à envoyer au client
     * @param authException L'exception d'authentification levée
     * @throws IOException En cas d'erreur lors de l'écriture de la réponse
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // Log minimal pour les erreurs d'authentification
        logger.warn("Tentative d'accès non autorisée détectée: {}", request.getRequestURI());
        // Détails supplémentaires uniquement en mode debug
        if (logger.isDebugEnabled()) {
            logger.debug("Type d'erreur: {}", authException.getClass().getSimpleName());
        }
        
        // Vérifier si l'erreur provient d'une ressource non trouvée (qui ne devrait pas être 401)
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            Object originalException = request.getAttribute("jakarta.servlet.error.exception");
            if (originalException != null && originalException.toString().contains("EntityNotFoundException")) {
                // C'est une erreur 404 déguisée, pas une erreur d'authentification
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Ressource non trouvée");
                return;
            }
        }
        
        // C'est une véritable erreur d'authentification
        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Non autorisé: " + authException.getMessage());
    }
    
    /**
     * Envoie une réponse d'erreur formatée en JSON au client.
     * 
     * @param response La réponse HTTP à envoyer au client
     * @param status Le code d'état HTTP à utiliser (401, 404, etc.)
     * @param message Le message d'erreur à inclure dans la réponse
     * @throws IOException En cas d'erreur lors de l'écriture de la réponse
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        
        ErrorResponse errorResponse = new ErrorResponse(
                status,
                message,
                RequestContextHolder.currentRequestAttributes().getAttribute("jakarta.servlet.forward.request_uri", 0).toString()
        );
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}