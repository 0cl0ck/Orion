package com.openclassrooms.mddapi.controllers;

import com.openclassrooms.mddapi.dto.CommentRequest;
import com.openclassrooms.mddapi.dto.CommentResponse;
import com.openclassrooms.mddapi.dto.MessageResponse;
import com.openclassrooms.mddapi.security.services.UserDetailsImpl;
import com.openclassrooms.mddapi.services.CommentService;
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
 * Contrôleur REST pour la gestion des commentaires
 */
@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * Récupère tous les commentaires d'un article
     * @param articleId Identifiant de l'article
     * @return Liste des commentaires de l'article
     */
    @GetMapping("/article/{articleId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByArticle(@PathVariable Long articleId) {
        return ResponseEntity.ok(commentService.getCommentsByArticle(articleId));
    }

    /**
     * Récupère tous les commentaires d'un utilisateur
     * @param userId Identifiant de l'utilisateur
     * @return Liste des commentaires de l'utilisateur
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(commentService.getCommentsByUser(userId));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Récupère un commentaire par son identifiant
     * @param id Identifiant du commentaire
     * @return Commentaire trouvé
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(commentService.getCommentById(id));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Crée un nouveau commentaire
     * @param commentRequest DTO contenant les données du commentaire à créer
     * @param userDetails Détails de l'utilisateur authentifié
     * @return Commentaire créé
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> createComment(
            @Valid @RequestBody CommentRequest commentRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            CommentResponse createdComment = commentService.createComment(commentRequest, userDetails.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Met à jour un commentaire existant
     * @param id Identifiant du commentaire à mettre à jour
     * @param commentRequest DTO contenant les données mises à jour
     * @param userDetails Détails de l'utilisateur authentifié
     * @return Commentaire mis à jour
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest commentRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            return ResponseEntity.ok(commentService.updateComment(id, commentRequest, userDetails.getId()));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Supprime un commentaire
     * @param id Identifiant du commentaire à supprimer
     * @param userDetails Détails de l'utilisateur authentifié
     * @return Message de confirmation
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            commentService.deleteComment(id, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Commentaire supprimé avec succès"));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }
}
