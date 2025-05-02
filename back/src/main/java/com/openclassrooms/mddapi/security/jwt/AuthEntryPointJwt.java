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

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        logger.error("Erreur d'authentification: {}", authException.getMessage());
        
        // Vérifier si l'erreur provient d'une ressource non trouvée (qui ne devrait pas être 401)
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            // Si l'exception originale est stockée dans les attributs de la requête,
            // on peut l'utiliser pour déterminer la vraie nature de l'erreur
            Object originalException = request.getAttribute("jakarta.servlet.error.exception");
            if (originalException != null && originalException.toString().contains("EntityNotFoundException")) {
                // C'est une erreur 404 déguisée, pas une erreur d'authentification
                logger.warn("Intercepté une EntityNotFoundException qui aurait été transformée en 401");
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Ressource non trouvée");
                return;
            }
        }
        
        // C'est une véritable erreur d'authentification
        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Non autorisé: " + authException.getMessage());
    }
    
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