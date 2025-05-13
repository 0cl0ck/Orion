package com.openclassrooms.mddapi.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Gère les IllegalArgumentException (souvent utilisées pour valider les données utilisateur)
     * et les transforme en réponses HTTP 400 (Bad Request)
     *
     * @param ex L'exception IllegalArgumentException lancée
     * @param request La requête Web
     * @return Une réponse avec le statut HTTP 400 et un message d'erreur approprié
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        logger.error("Argument invalide: {}", ex.getMessage());
        logger.debug("Détails complets de l'exception:", ex);
        
        // Message convivial pour les erreurs utilisateur courantes
        String message = ex.getMessage();
        if (message != null) {
            if (message.contains("nom d'utilisateur est déjà utilisé")) {
                message = "Ce nom d'utilisateur est déjà utilisé par un autre compte.";
            } 
            // Détection plus précise pour les emails
            else if (message.contains("email est déjà utilisé") || message.contains("Cet email est déjà utilisé")) {
                message = "Cette adresse email est déjà associée à un autre compte.";
            }
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message,
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Gestionnaire pour toutes les exceptions non spécifiquement traitées
     * Cela inclut les exceptions ResponseStatusException lancées par les contrôleurs
     *
     * @param ex L'exception générale
     * @param request La requête web
     * @return Une réponse avec le statut HTTP approprié et un message d'erreur
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        
        // Détermine le statut HTTP et message en fonction du type d'exception
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // Par défaut
        String message = ex.getMessage();
        String exceptionName = ex.getClass().getSimpleName();
        
        // Logging structuré
        logger.error("Exception {} : {}", exceptionName, message);
        if (status.is5xxServerError()) {
            logger.error("Détails complets de l'exception:", ex);
        } else {
            logger.debug("Détails de l'exception:", ex);
        }
        
        // Gérer les ResponseStatusException spécifiquement
        if (ex instanceof org.springframework.web.server.ResponseStatusException) {
            org.springframework.web.server.ResponseStatusException rse = 
                    (org.springframework.web.server.ResponseStatusException) ex;
            try {
                status = HttpStatus.valueOf(rse.getStatusCode().value());
                message = rse.getReason();
            } catch (Exception e) {
                logger.warn("Impossible de lire le statut HTTP de ResponseStatusException", e);
            }
        }

        // Formater des messages d'erreur conviviaux pour certains cas courants
        if (message != null) {
            if (message.contains("nom d'utilisateur est déjà utilisé")) {
                message = "Ce nom d'utilisateur est déjà utilisé par un autre compte.";
                status = HttpStatus.BAD_REQUEST;
            } 
            // Détection plus précise des messages d'erreur liés aux emails
            else if (message.contains("email est déjà utilisé") || message.contains("Cet email est déjà utilisé")) {
                message = "Cette adresse email est déjà associée à un autre compte.";
                status = HttpStatus.BAD_REQUEST;
            } else if (message.contains("Utilisateur non trouvé")) {
                status = HttpStatus.NOT_FOUND;
            } else if (message.contains("autorisé") || message.contains("authentifié")) {
                status = HttpStatus.FORBIDDEN;
            }
        }
        
        // Message par défaut si aucun message n'est disponible ou pour les 500
        if (message == null || message.isEmpty() || status.is5xxServerError()) {
            message = "Une erreur est survenue sur le serveur. Veuillez réessayer plus tard.";
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                message,
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Gère les exceptions liées aux URL non trouvées (404 - Not Found)
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex, WebRequest request) {
        logger.error("Ressource non trouvée: {}", request.getDescription(false));
        logger.debug("Détails de l'exception:", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "La ressource demandée n'existe pas: " + request.getDescription(false),
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Gère les exceptions liées aux méthodes HTTP non supportées
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        
        logger.error("Méthode non supportée: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Méthode HTTP non supportée: " + ex.getMessage(),
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }
    
    /**
     * Gère les exceptions liées au format de données invalide
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        logger.error("Format de données invalide: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Format de données invalide: veuillez vérifier votre requête",
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Gère les erreurs de validation des arguments de méthode (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String errorMessage = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
        
        logger.error("Erreur de validation: {}", errorMessage);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation échouée: " + errorMessage,
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Gère les erreurs de validation de contraintes
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        String errorMessage = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        logger.error("Violation de contrainte: {}", errorMessage);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation échouée: " + errorMessage,
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // La méthode handleGlobalException a été fusionnée avec celle du haut du fichier
}
