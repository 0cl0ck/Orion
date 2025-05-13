package com.openclassrooms.mddapi.controllers;

import com.openclassrooms.mddapi.dto.ThemeRequest;
import com.openclassrooms.mddapi.dto.ThemeResponse;
import com.openclassrooms.mddapi.security.services.UserDetailsImpl;
import com.openclassrooms.mddapi.services.ThemeService;
import com.openclassrooms.mddapi.services.UserThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openclassrooms.mddapi.exceptions.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/themes")
@Tag(name = "Thèmes", description = "API de gestion des thèmes")
public class ThemeController {

    private static final Logger logger = LoggerFactory.getLogger(ThemeController.class);

    @Autowired
    private ThemeService themeService;
    
    @Autowired
    private UserThemeService userThemeService;
    
    @GetMapping("/subscriptions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Récupérer les thèmes auxquels l'utilisateur est abonné", 
              description = "Retourne la liste des IDs des thèmes auxquels l'utilisateur connecté est abonné")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des IDs des thèmes récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non autorisé", 
                         content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erreur serveur", 
                         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getUserSubscriptions(HttpServletRequest request) {
        try {
            // Récupérer l'ID de l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            
            // Récupérer les IDs des thèmes auxquels l'utilisateur est abonné
            List<Long> subscribedThemeIds = userThemeService.getSubscribedThemeIds(userId);
            
            logger.info("Récupération des thèmes auxquels l'utilisateur {} est abonné. Nombre: {}", 
                       userId, subscribedThemeIds.size());
            
            return ResponseEntity.ok(subscribedThemeIds);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des abonnements: {}", e.getMessage());
            
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erreur lors de la récupération des abonnements: " + e.getMessage(),
                request.getRequestURI()
            );
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les thèmes", description = "Retourne la liste de tous les thèmes disponibles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des thèmes récupérée avec succès",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ThemeResponse.class)) })
    })
    public ResponseEntity<List<ThemeResponse>> getAllThemes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<ThemeResponse> themes = themeService.getAllThemes();
        
        // Si l'utilisateur est authentifié, marquer les thèmes auxquels il est abonné
        if (authentication != null && authentication.isAuthenticated() && 
                authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            
            List<Long> subscribedThemeIds = userThemeService.getSubscribedThemeIds(userId);
            
            // Mettre à jour le statut d'abonnement de chaque thème
            themes.forEach(theme -> {
                theme.setSubscribed(subscribedThemeIds.contains(theme.getId()));
            });
        }
        
        return new ResponseEntity<>(themes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un thème par ID", description = "Retourne un thème en fonction de son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thème trouvé",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ThemeResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Thème non trouvé", content = @Content)
    })
    public ResponseEntity<?> getThemeById(
            @Parameter(description = "ID du thème à récupérer") @PathVariable Long id,
            HttpServletRequest request) {
        
        logger.info("Récupération du thème avec l'ID: {}", id);
        
        // Vérification préalable de l'existence du thème
        boolean themeExists = themeService.existsById(id);
        if (!themeExists) {
            logger.warn("Thème non trouvé avec l'ID: {}", id);
            
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Thème non trouvé avec l'id: " + id,
                request.getRequestURI()
            );
            
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
        }
        
        // Le thème existe, continuer avec le service
        ThemeResponse theme = themeService.getThemeById(id);
        
        // Vérifier si l'utilisateur est abonné à ce thème
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
                authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            theme.setSubscribed(userThemeService.isUserSubscribedToTheme(userId, id));
        }
        
        return new ResponseEntity<>(theme, HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Créer un nouveau thème", description = "Crée un nouveau thème avec les données fournies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Thème créé avec succès",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ThemeResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content)
    })
    public ResponseEntity<ThemeResponse> createTheme(
            @Parameter(description = "Données du nouveau thème") @Valid @RequestBody ThemeRequest themeRequest) {
        try {
            ThemeResponse createdTheme = themeService.createTheme(themeRequest);
            return new ResponseEntity<>(createdTheme, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erreur lors de la création du thème: " + e.getMessage(), e);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mettre à jour un thème", description = "Met à jour les données d'un thème existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thème mis à jour avec succès",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ThemeResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Thème non trouvé", content = @Content),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content)
    })
    public ResponseEntity<?> updateTheme(
            @Parameter(description = "ID du thème à mettre à jour") @PathVariable Long id,
            @Parameter(description = "Nouvelles données du thème") @Valid @RequestBody ThemeRequest themeRequest,
            HttpServletRequest request) {
        
        logger.info("Mise à jour du thème avec l'ID: {}", id);
        logger.info("Nouvelles données: {}", themeRequest);
        
        // Vérification préalable de l'existence du thème
        boolean themeExists = themeService.existsById(id);
        if (!themeExists) {
            logger.warn("Thème non trouvé avec l'ID: {}", id);
            
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Thème non trouvé avec l'id: " + id,
                request.getRequestURI()
            );
            
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
        }
        
        try {
            ThemeResponse updatedTheme = themeService.updateTheme(id, themeRequest);
            return new ResponseEntity<>(updatedTheme, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du thème: {}", e.getMessage());
            
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erreur lors de la mise à jour du thème: " + e.getMessage(),
                request.getRequestURI()
            );
            
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Supprimer un thème", description = "Supprime un thème en fonction de son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Thème supprimé avec succès", content = @Content),
            @ApiResponse(responseCode = "404", description = "Thème non trouvé", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content)
    })
    public ResponseEntity<?> deleteTheme(
            @Parameter(description = "ID du thème à supprimer") @PathVariable Long id,
            HttpServletRequest request) {
        
        logger.info("Suppression du thème avec l'ID: {}", id);
        
        // Vérification préalable de l'existence du thème
        boolean themeExists = themeService.existsById(id);
        if (!themeExists) {
            logger.warn("Thème non trouvé avec l'ID: {}", id);
            
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Thème non trouvé avec l'id: " + id,
                request.getRequestURI()
            );
            
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
        }
        
        try {
            themeService.deleteTheme(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du thème: {}", e.getMessage());
            
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erreur lors de la suppression du thème: " + e.getMessage(),
                request.getRequestURI()
            );
            
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
        }
    }
    
    @PostMapping("/{id}/subscribe")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "S'abonner à un thème", description = "Permet à l'utilisateur connecté de s'abonner à un thème")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Abonnement réussi"),
            @ApiResponse(responseCode = "404", description = "Thème non trouvé"),
            @ApiResponse(responseCode = "400", description = "Erreur lors de l'abonnement"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<?> subscribeToTheme(
            @Parameter(description = "ID du thème auquel s'abonner") @PathVariable Long id,
            HttpServletRequest request) {
        
        logger.info("Demande d'abonnement au thème avec l'ID: {}", id);
        
        // Vérification de l'existence du thème
        boolean themeExists = themeService.existsById(id);
        if (!themeExists) {
            logger.warn("Thème non trouvé avec l'ID: {}", id);
            
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Thème non trouvé avec l'id: " + id,
                request.getRequestURI()
            );
            
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
        }
        
        try {
            // Récupérer l'ID de l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            
            // Abonner l'utilisateur au thème
            boolean result = userThemeService.subscribeUserToTheme(userId, id);
            
            Map<String, Object> response = new HashMap<>();
            if (result) {
                response.put("success", true);
                response.put("message", "Abonnement réussi");
                logger.info("Utilisateur {} abonné au thème {}", userId, id);
            } else {
                response.put("success", false);
                response.put("message", "Vous êtes déjà abonné à ce thème");
                logger.info("L'utilisateur {} est déjà abonné au thème {}", userId, id);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de l'abonnement au thème: {}", e.getMessage());
            
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erreur lors de l'abonnement au thème: " + e.getMessage(),
                request.getRequestURI()
            );
            
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
        }
    }
    
    @PostMapping("/{id}/unsubscribe")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Se désabonner d'un thème", description = "Permet à l'utilisateur connecté de se désabonner d'un thème")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Désabonnement réussi"),
            @ApiResponse(responseCode = "404", description = "Thème non trouvé"),
            @ApiResponse(responseCode = "400", description = "Erreur lors du désabonnement"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<?> unsubscribeFromTheme(
            @Parameter(description = "ID du thème auquel se désabonner") @PathVariable Long id,
            HttpServletRequest request) {
        
        logger.info("Demande de désabonnement du thème avec l'ID: {}", id);
        
        // Vérification de l'existence du thème
        boolean themeExists = themeService.existsById(id);
        if (!themeExists) {
            logger.warn("Thème non trouvé avec l'ID: {}", id);
            
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Thème non trouvé avec l'id: " + id,
                request.getRequestURI()
            );
            
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
        }
        
        try {
            // Récupérer l'ID de l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            
            // Désabonner l'utilisateur du thème
            boolean result = userThemeService.unsubscribeUserFromTheme(userId, id);
            
            Map<String, Object> response = new HashMap<>();
            if (result) {
                response.put("success", true);
                response.put("message", "Désabonnement réussi");
                logger.info("Utilisateur {} désabonné du thème {}", userId, id);
            } else {
                response.put("success", false);
                response.put("message", "Vous n'êtes pas abonné à ce thème");
                logger.info("L'utilisateur {} n'est pas abonné au thème {}", userId, id);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors du désabonnement du thème: {}", e.getMessage());
            
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erreur lors du désabonnement du thème: " + e.getMessage(),
                request.getRequestURI()
            );
            
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
        }
    }
}