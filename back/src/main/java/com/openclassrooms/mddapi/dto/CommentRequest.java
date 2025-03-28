package com.openclassrooms.mddapi.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la création et la mise à jour d'un commentaire
 */
@Data
public class CommentRequest {
    @NotBlank(message = "Le contenu est obligatoire")
    @Size(min = 2, max = 500, message = "Le commentaire doit contenir entre 2 et 500 caractères")
    private String content;
    
    @NotNull(message = "L'identifiant de l'article est obligatoire")
    private Long articleId;
}
