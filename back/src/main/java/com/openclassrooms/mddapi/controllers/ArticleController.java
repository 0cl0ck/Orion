package com.openclassrooms.mddapi.controllers;

import com.openclassrooms.mddapi.dto.ArticleRequest;
import com.openclassrooms.mddapi.dto.ArticleResponse;
import com.openclassrooms.mddapi.dto.MessageResponse;
import com.openclassrooms.mddapi.security.services.UserDetailsImpl;
import com.openclassrooms.mddapi.services.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des articles
 */
@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Articles", description = "API de gestion des articles")
public class ArticleController {

    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleService articleService;

    /**
     * Récupère tous les articles
     * @return Liste des articles
     */
    @Operation(summary = "Récupérer tous les articles", description = "Retourne la liste de tous les articles disponibles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des articles récupérée avec succès",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleResponse.class)) })
    })
    @GetMapping
    public ResponseEntity<List<ArticleResponse>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    /**
     * Récupère un article par son identifiant
     * @param id Identifiant de l'article
     * @return Article trouvé
     */
    @Operation(summary = "Récupérer un article par ID", description = "Retourne un article en fonction de son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Article trouvé",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Article non trouvé", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getArticleById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(articleService.getArticleById(id));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Récupère les articles d'un thème spécifique
     * @param themeId Identifiant du thème
     * @return Liste des articles du thème
     */
    @Operation(summary = "Récupérer les articles d'un thème", description = "Retourne la liste des articles d'un thème spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des articles récupérée avec succès",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Thème non trouvé", content = @Content)
    })
    @GetMapping("/theme/{themeId}")
    public ResponseEntity<List<ArticleResponse>> getArticlesByTheme(@PathVariable Long themeId) {
        try {
            return ResponseEntity.ok(articleService.getArticlesByTheme(themeId));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Récupère les articles d'un utilisateur spécifique
     * @param userId Identifiant de l'utilisateur
     * @return Liste des articles de l'utilisateur
     */
    @Operation(summary = "Récupérer les articles d'un utilisateur", description = "Retourne la liste des articles d'un utilisateur spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des articles récupérée avec succès",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé", content = @Content)
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ArticleResponse>> getArticlesByUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(articleService.getArticlesByUser(userId));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Recherche des articles par titre
     * @param title Titre à rechercher
     * @return Liste des articles correspondants
     */
    @Operation(summary = "Rechercher des articles par titre", description = "Retourne la liste des articles dont le titre correspond à la recherche")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des articles récupérée avec succès",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleResponse.class)) })
    })
    @GetMapping("/search")
    public ResponseEntity<List<ArticleResponse>> searchArticlesByTitle(@RequestParam String title) {
        return ResponseEntity.ok(articleService.searchArticlesByTitle(title));
    }

    /**
     * Crée un nouvel article
     * @param articleRequest DTO contenant les données de l'article à créer
     * @param userDetails Détails de l'utilisateur authentifié
     * @return Article créé
     */
    @Operation(summary = "Créer un nouvel article", description = "Crée un nouvel article avec les données fournies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Article créé avec succès",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content)
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ArticleResponse> createArticle(
            @Valid @RequestBody ArticleRequest articleRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        logger.info("========== DÉBUT DE CRÉATION D'ARTICLE ==========");
        
        // Vérifier l'authentification
        if (userDetails == null) {
            logger.error("userDetails est NULL - l'utilisateur n'est pas correctement authentifié");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        } else {
            logger.info("Utilisateur authentifié: ID={}, Username={}", userDetails.getId(), userDetails.getUsername());
            logger.info("Autorités/Rôles de l'utilisateur: {}", userDetails.getAuthorities());
        }
        
        // Vérifier les données de la requête
        logger.info("Données de l'article: {}", articleRequest);
        logger.info("  - Titre: {}", articleRequest.getTitle());
        logger.info("  - Contenu: {} (longueur: {})", 
            (articleRequest.getContent() != null ? articleRequest.getContent().substring(0, Math.min(30, articleRequest.getContent().length())) + "..." : "null"),
            (articleRequest.getContent() != null ? articleRequest.getContent().length() : 0));
        logger.info("  - ThemeId: {}", articleRequest.getThemeId());
        
        try {
            logger.info("Appel du service pour créer l'article...");
            ArticleResponse createdArticle = articleService.createArticle(articleRequest, userDetails.getId());
            logger.info("Article créé avec succès! ID={}", createdArticle.getId());
            logger.info("========== FIN DE CRÉATION D'ARTICLE (SUCCÈS) ==========");
            return ResponseEntity.status(HttpStatus.CREATED).body(createdArticle);
        } catch (EntityNotFoundException e) {
            logger.error("Erreur lors de la création de l'article: {}", e.getMessage());
            logger.error("Exception complète:", e);
            logger.info("========== FIN DE CRÉATION D'ARTICLE (ÉCHEC) ==========");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Exception non prévue lors de la création de l'article: {}", e.getMessage());
            logger.error("Exception complète:", e);
            logger.info("========== FIN DE CRÉATION D'ARTICLE (ÉCHEC) ==========");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la création de l'article");
        }
    }

    /**
     * Met à jour un article existant
     * @param id Identifiant de l'article à mettre à jour
     * @param articleRequest DTO contenant les données mises à jour
     * @param userDetails Détails de l'utilisateur authentifié
     * @return Article mis à jour
     */
    @Operation(summary = "Mettre à jour un article", description = "Met à jour les données d'un article existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Article mis à jour avec succès",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Article non trouvé", content = @Content),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ArticleResponse> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequest articleRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            return ResponseEntity.ok(articleService.updateArticle(id, articleRequest, userDetails.getId()));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Supprime un article
     * @param id Identifiant de l'article à supprimer
     * @param userDetails Détails de l'utilisateur authentifié
     * @return Message de confirmation
     */
    @Operation(summary = "Supprimer un article", description = "Supprime un article en fonction de son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Article supprimé avec succès", content = @Content),
            @ApiResponse(responseCode = "404", description = "Article non trouvé", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> deleteArticle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            articleService.deleteArticle(id, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Article supprimé avec succès"));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }
}
