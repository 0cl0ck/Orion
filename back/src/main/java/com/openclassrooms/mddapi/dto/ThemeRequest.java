package com.openclassrooms.mddapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO pour la création et la mise à jour d'un thème
 */
@Data
public class ThemeRequest {
    
    @NotBlank(message = "Le nom du thème est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom du thème doit contenir entre 2 et 50 caractères")
    private String name;
    
    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères")
    private String description;
}
