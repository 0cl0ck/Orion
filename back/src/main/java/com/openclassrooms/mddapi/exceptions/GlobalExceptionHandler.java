package com.openclassrooms.mddapi.exceptions;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Gestionnaire global d'exceptions pour assurer une gestion cohérente
 * et correcte des erreurs à travers l'application.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Gère les exceptions EntityNotFoundException et les transforme en réponses HTTP 404 (Not Found)
     * au lieu de les laisser être potentiellement interceptées par Spring Security comme des 401.
     *
     * @param ex L'exception EntityNotFoundException lancée
     * @param request La requête Web
     * @return Une réponse avec le statut HTTP 404 et un message d'erreur
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex, WebRequest request) {
        
        logger.error("Entité non trouvée: {}", ex.getMessage());
        logger.debug("Détails complets de l'exception:", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Gère les IllegalStateException (souvent utilisées pour les violations d'accès)
     * et les transforme en réponses HTTP 403 (Forbidden)
     *
     * @param ex L'exception IllegalStateException lancée
     * @param request La requête Web
     * @return Une réponse avec le statut HTTP 403 et un message d'erreur
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {
        
        logger.error("État illégal: {}", ex.getMessage());
        logger.debug("Détails complets de l'exception:", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Gestionnaire pour toutes les autres exceptions non spécifiquement traitées
     *
     * @param ex L'exception lancée
     * @param request La requête Web
     * @return Une réponse avec le statut HTTP 500 et un message d'erreur
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        
        logger.error("Exception non gérée: {}", ex.getMessage());
        logger.error("Détails complets de l'exception:", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Une erreur interne s'est produite. Veuillez réessayer plus tard.",
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
