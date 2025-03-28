package com.openclassrooms.mddapi.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la création et la mise à jour d'un article
 */
@Data
public class ArticleRequest {
    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 3, max = 100, message = "Le titre doit contenir entre 3 et 100 caractères")
    private String title;
    
    @NotBlank(message = "Le contenu est obligatoire")
    @Size(min = 10, message = "Le contenu doit contenir au moins 10 caractères")
    private String content;
    
    @NotNull(message = "L'identifiant du thème est obligatoire")
    private Long themeId;
}
