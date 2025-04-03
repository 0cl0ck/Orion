package com.openclassrooms.mddapi.controllers;

import com.openclassrooms.mddapi.dto.ThemeRequest;
import com.openclassrooms.mddapi.dto.ThemeResponse;
import com.openclassrooms.mddapi.services.ThemeService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/themes")
@Tag(name = "Thèmes", description = "API de gestion des thèmes")
public class ThemeController {

    @Autowired
    private ThemeService themeService;

    @GetMapping
    @Operation(summary = "Récupérer tous les thèmes", description = "Retourne la liste de tous les thèmes disponibles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des thèmes récupérée avec succès",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ThemeResponse.class)) })
    })
    public ResponseEntity<List<ThemeResponse>> getAllThemes() {
        List<ThemeResponse> themes = themeService.getAllThemes();
        return new ResponseEntity<>(themes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un thème par ID", description = "Retourne un thème en fonction de son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thème trouvé",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ThemeResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Thème non trouvé", content = @Content)
    })
    public ResponseEntity<ThemeResponse> getThemeById(
            @Parameter(description = "ID du thème à récupérer") @PathVariable Long id) {
        try {
            ThemeResponse theme = themeService.getThemeById(id);
            return new ResponseEntity<>(theme, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Thème non trouvé avec l'id: " + id, e);
        }
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
    public ResponseEntity<ThemeResponse> updateTheme(
            @Parameter(description = "ID du thème à mettre à jour") @PathVariable Long id,
            @Parameter(description = "Nouvelles données du thème") @Valid @RequestBody ThemeRequest themeRequest) {
        try {
            ThemeResponse updatedTheme = themeService.updateTheme(id, themeRequest);
            return new ResponseEntity<>(updatedTheme, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    e.getClass().getSimpleName().equals("EntityNotFoundException") ? 
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST, 
                    e.getMessage(), e
            );
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
    public ResponseEntity<Void> deleteTheme(
            @Parameter(description = "ID du thème à supprimer") @PathVariable Long id) {
        try {
            themeService.deleteTheme(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    e.getClass().getSimpleName().equals("EntityNotFoundException") ? 
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST, 
                    e.getMessage(), e
            );
        }
    }
}