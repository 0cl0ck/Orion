package com.openclassrooms.mddapi.controllers;

import com.openclassrooms.mddapi.dto.MessageResponse;
import com.openclassrooms.mddapi.dto.UserRequest;
import com.openclassrooms.mddapi.dto.UserResponse;
import com.openclassrooms.mddapi.security.services.UserDetailsImpl;
import com.openclassrooms.mddapi.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des utilisateurs
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
@Tag(name = "Utilisateurs", description = "API de gestion des utilisateurs")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Récupère tous les utilisateurs
     * @return Liste de tous les utilisateurs
     */
    @GetMapping
    @Operation(summary = "Récupérer tous les utilisateurs", description = "Retourne la liste de tous les utilisateurs enregistrés")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des utilisateurs récupérée avec succès",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class)) })
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Récupère un utilisateur par son ID
     * @param id Identifiant de l'utilisateur
     * @return Utilisateur trouvé
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un utilisateur par ID", description = "Retourne un utilisateur en fonction de son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé", content = @Content)
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID de l'utilisateur à récupérer") @PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Récupère un utilisateur par son nom d'utilisateur
     * @param username Nom d'utilisateur
     * @return Utilisateur trouvé
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "Récupérer un utilisateur par nom d'utilisateur", description = "Retourne un utilisateur en fonction de son nom d'utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé", content = @Content)
    })
    public ResponseEntity<UserResponse> getUserByUsername(
            @Parameter(description = "Nom d'utilisateur de l'utilisateur à récupérer") @PathVariable String username) {
        try {
            return ResponseEntity.ok(userService.getUserByUsername(username));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Met à jour les informations d'un utilisateur
     * @param id Identifiant de l'utilisateur à mettre à jour
     * @param userRequest DTO contenant les données mises à jour
     * @param userDetails Détails de l'utilisateur authentifié
     * @return Utilisateur mis à jour
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mettre à jour un utilisateur", description = "Met à jour les données d'un utilisateur existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur mis à jour avec succès",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé", content = @Content),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content)
    })
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "ID de l'utilisateur à mettre à jour") @PathVariable Long id,
            @Parameter(description = "Nouvelles données de l'utilisateur") @Valid @RequestBody UserRequest userRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            return ResponseEntity.ok(userService.updateUser(id, userRequest, userDetails.getId()));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Supprime un utilisateur
     * @param id Identifiant de l'utilisateur à supprimer
     * @param userDetails Détails de l'utilisateur authentifié
     * @return Message de confirmation
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Supprimer un utilisateur", description = "Supprime un utilisateur en fonction de son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Utilisateur supprimé avec succès", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès refusé", content = @Content)
    })
    public ResponseEntity<MessageResponse> deleteUser(
            @Parameter(description = "ID de l'utilisateur à supprimer") @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            userService.deleteUser(id, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Utilisateur supprimé avec succès"));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Vérifie si un nom d'utilisateur existe déjà
     * @param username Nom d'utilisateur à vérifier
     * @return true si le nom d'utilisateur existe, false sinon
     */
    @GetMapping("/check/username/{username}")
    @Operation(summary = "Vérifier si un nom d'utilisateur existe", description = "Retourne true si le nom d'utilisateur existe, false sinon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nom d'utilisateur vérifié",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)) })
    })
    public ResponseEntity<Boolean> checkUsernameExists(
            @Parameter(description = "Nom d'utilisateur à vérifier") @PathVariable String username) {
        return ResponseEntity.ok(userService.existsByUsername(username));
    }

    /**
     * Vérifie si un email existe déjà
     * @param email Email à vérifier
     * @return true si l'email existe, false sinon
     */
    @GetMapping("/check/email/{email}")
    @Operation(summary = "Vérifier si un email existe", description = "Retourne true si l'email existe, false sinon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email vérifié",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)) })
    })
    public ResponseEntity<Boolean> checkEmailExists(
            @Parameter(description = "Email à vérifier") @PathVariable String email) {
        return ResponseEntity.ok(userService.existsByEmail(email));
    }

    /**
     * Récupère le profil de l'utilisateur actuellement connecté
     * @param userDetails Détails de l'utilisateur authentifié
     * @return Profil de l'utilisateur
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Récupérer le profil de l'utilisateur actuellement connecté", description = "Retourne le profil de l'utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil récupéré",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé", content = @Content)
    })
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            return ResponseEntity.ok(userService.getUserById(userDetails.getId()));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
