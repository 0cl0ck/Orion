package com.openclassrooms.mddapi.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO pour la réponse contenant les données d'un utilisateur
 * Ne contient pas les informations sensibles comme le mot de passe
 */
@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer articleCount;
    private Integer commentCount;
}
