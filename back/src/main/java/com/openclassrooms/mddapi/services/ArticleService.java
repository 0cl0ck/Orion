package com.openclassrooms.mddapi.services;

import com.openclassrooms.mddapi.dto.ArticleRequest;
import com.openclassrooms.mddapi.dto.ArticleResponse;
import com.openclassrooms.mddapi.models.Article;
import com.openclassrooms.mddapi.models.Theme;
import com.openclassrooms.mddapi.models.User;
import com.openclassrooms.mddapi.repositories.ArticleRepository;
import com.openclassrooms.mddapi.repositories.ThemeRepository;
import com.openclassrooms.mddapi.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import com.openclassrooms.mddapi.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des articles
 */
@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Récupère tous les articles par ordre décroissant de date de création
     * @return Liste des articles transformés en DTO de réponse
     */
    public List<ArticleResponse> getAllArticles() {
        return articleRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToArticleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un article par son identifiant
     * @param id Identifiant de l'article
     * @return DTO de réponse contenant les données de l'article
     * @throws EntityNotFoundException si l'article n'existe pas
     */
    public ArticleResponse getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé avec l'id : " + id));
        return mapToArticleResponse(article);
    }

    /**
     * Récupère les articles d'un thème spécifique
     * @param themeId Identifiant du thème
     * @return Liste des articles du thème transformés en DTO de réponse
     */
    public List<ArticleResponse> getArticlesByTheme(Long themeId) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new EntityNotFoundException("Thème non trouvé avec l'id : " + themeId));
        
        return articleRepository.findByTheme(theme)
                .stream()
                .map(this::mapToArticleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les articles d'un utilisateur spécifique
     * @param userId Identifiant de l'utilisateur
     * @return Liste des articles de l'utilisateur transformés en DTO de réponse
     */
    public List<ArticleResponse> getArticlesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id : " + userId));
        
        return articleRepository.findByAuthor(user)
                .stream()
                .map(this::mapToArticleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Recherche des articles par titre
     * @param title Titre à rechercher
     * @return Liste des articles correspondants transformés en DTO de réponse
     */
    public List<ArticleResponse> searchArticlesByTitle(String title) {
        return articleRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::mapToArticleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Crée un nouvel article
     * @param articleRequest DTO contenant les données de l'article à créer
     * @param userId Identifiant de l'utilisateur créant l'article
     * @return DTO de réponse contenant les données de l'article créé
     * @throws ResourceNotFoundException si le thème ou l'utilisateur n'existe pas
     */
    @Transactional
    public ArticleResponse createArticle(ArticleRequest articleRequest, Long userId) {
        // Vérifier que l'utilisateur existe
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));
        
        // Vérifier que le thème existe (utilise notre exception personnalisée)
        Theme theme = themeRepository.findById(articleRequest.getThemeId())
                .orElseThrow(() -> new ResourceNotFoundException("Thème", "id", articleRequest.getThemeId()));
        
        Article article = new Article();
        article.setTitle(articleRequest.getTitle());
        article.setContent(articleRequest.getContent());
        article.setAuthor(author);
        article.setTheme(theme);
        
        Article savedArticle = articleRepository.save(article);
        return mapToArticleResponse(savedArticle);
    }

    /**
     * Met à jour un article existant
     * @param id Identifiant de l'article à mettre à jour
     * @param articleRequest DTO contenant les données mises à jour
     * @param userId Identifiant de l'utilisateur effectuant la mise à jour
     * @return DTO de réponse contenant les données de l'article mis à jour
     * @throws EntityNotFoundException si l'article, le thème ou l'utilisateur n'existe pas
     * @throws IllegalStateException si l'utilisateur n'est pas l'auteur de l'article
     */
    @Transactional
    public ArticleResponse updateArticle(Long id, ArticleRequest articleRequest, Long userId) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé avec l'id : " + id));
        
        // Vérifier que l'utilisateur est bien l'auteur de l'article
        if (!article.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("Seul l'auteur peut modifier cet article");
        }
        
        Theme theme = themeRepository.findById(articleRequest.getThemeId())
                .orElseThrow(() -> new EntityNotFoundException("Thème non trouvé avec l'id : " + articleRequest.getThemeId()));
        
        article.setTitle(articleRequest.getTitle());
        article.setContent(articleRequest.getContent());
        article.setTheme(theme);
        
        Article updatedArticle = articleRepository.save(article);
        return mapToArticleResponse(updatedArticle);
    }

    /**
     * Supprime un article
     * @param id Identifiant de l'article à supprimer
     * @param userId Identifiant de l'utilisateur effectuant la suppression
     * @throws EntityNotFoundException si l'article n'existe pas
     * @throws IllegalStateException si l'utilisateur n'est pas l'auteur de l'article
     */
    @Transactional
    public void deleteArticle(Long id, Long userId) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé avec l'id : " + id));
        
        // Vérifier que l'utilisateur est bien l'auteur de l'article
        if (!article.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("Seul l'auteur peut supprimer cet article");
        }
        
        articleRepository.delete(article);
    }

    /**
     * Transforme une entité Article en DTO de réponse
     * @param article Entité Article à transformer
     * @return DTO de réponse contenant les données de l'article
     */
    private ArticleResponse mapToArticleResponse(Article article) {
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .createdAt(article.getCreatedAt())
                .authorId(article.getAuthor().getId())
                .authorUsername(article.getAuthor().getUsername())
                .themeId(article.getTheme().getId())
                .themeName(article.getTheme().getName())
                .commentCount(article.getComments() != null ? article.getComments().size() : 0)
                .build();
    }
}
