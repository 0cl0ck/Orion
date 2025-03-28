package com.openclassrooms.mddapi.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO pour la réponse contenant les données d'un thème
 */
@Data
@Builder
public class ThemeResponse {
    private Long id;
    private String name;
    private String description;
    private Integer articleCount;
}
