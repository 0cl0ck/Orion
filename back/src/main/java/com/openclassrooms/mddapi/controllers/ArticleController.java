package com.openclassrooms.mddapi.controllers;

import com.openclassrooms.mddapi.dto.ArticleRequest;
import com.openclassrooms.mddapi.dto.ArticleResponse;
import com.openclassrooms.mddapi.dto.MessageResponse;
import com.openclassrooms.mddapi.security.services.UserDetailsImpl;
import com.openclassrooms.mddapi.services.ArticleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
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
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * Récupère tous les articles
     * @return Liste des articles
     */
    @GetMapping
    public ResponseEntity<List<ArticleResponse>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    /**
     * Récupère un article par son identifiant
     * @param id Identifiant de l'article
     * @return Article trouvé
     */
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
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ArticleResponse> createArticle(
            @Valid @RequestBody ArticleRequest articleRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            ArticleResponse createdArticle = articleService.createArticle(articleRequest, userDetails.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdArticle);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Met à jour un article existant
     * @param id Identifiant de l'article à mettre à jour
     * @param articleRequest DTO contenant les données mises à jour
     * @param userDetails Détails de l'utilisateur authentifié
     * @return Article mis à jour
     */
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
