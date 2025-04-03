package com.openclassrooms.mddapi.controllers;

import com.openclassrooms.mddapi.dto.JwtResponse;
import com.openclassrooms.mddapi.dto.LoginRequest;
import com.openclassrooms.mddapi.dto.MessageResponse;
import com.openclassrooms.mddapi.dto.RegisterRequest;
import com.openclassrooms.mddapi.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "API d'authentification des utilisateurs")
public class AuthController {
    
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authentifier un utilisateur", description = "Authentifie un utilisateur et génère un token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentification réussie",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Informations d'authentification invalides", 
                    content = @Content)
    })
    public ResponseEntity<?> authenticateUser(
            @Parameter(description = "Informations de connexion") @Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur d'authentification: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Enregistrer un nouvel utilisateur", description = "Crée un compte utilisateur avec les informations fournies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur enregistré avec succès",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Informations d'enregistrement invalides ou déjà utilisées", 
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)) })
    })
    public ResponseEntity<?> registerUser(
            @Parameter(description = "Informations d'enregistrement") @Valid @RequestBody RegisterRequest registerRequest) {
        try {
            authService.registerUser(registerRequest);
            return ResponseEntity.ok(new MessageResponse("Utilisateur enregistré avec succès!"));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(new MessageResponse(e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur lors de l'enregistrement: " + e.getMessage()));
        }
    }
}