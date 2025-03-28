package com.openclassrooms.mddapi.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO pour la réponse contenant les données d'un article
 */
@Data
@Builder
public class ArticleResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private Long authorId;
    private String authorUsername;
    private Long themeId;
    private String themeName;
    private int commentCount;
}
