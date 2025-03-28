package com.openclassrooms.mddapi.services;

import com.openclassrooms.mddapi.dto.CommentRequest;
import com.openclassrooms.mddapi.dto.CommentResponse;
import com.openclassrooms.mddapi.models.Article;
import com.openclassrooms.mddapi.models.Comment;
import com.openclassrooms.mddapi.models.User;
import com.openclassrooms.mddapi.repositories.ArticleRepository;
import com.openclassrooms.mddapi.repositories.CommentRepository;
import com.openclassrooms.mddapi.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des commentaires
 */
@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Récupère tous les commentaires d'un article
     * @param articleId Identifiant de l'article
     * @return Liste des commentaires transformés en DTO de réponse
     */
    public List<CommentResponse> getCommentsByArticle(Long articleId) {
        return commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId)
                .stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère tous les commentaires d'un utilisateur
     * @param userId Identifiant de l'utilisateur
     * @return Liste des commentaires transformés en DTO de réponse
     */
    public List<CommentResponse> getCommentsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id : " + userId));
        
        return commentRepository.findByAuthor(user)
                .stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un commentaire par son identifiant
     * @param id Identifiant du commentaire
     * @return DTO de réponse contenant les données du commentaire
     * @throws EntityNotFoundException si le commentaire n'existe pas
     */
    public CommentResponse getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commentaire non trouvé avec l'id : " + id));
        return mapToCommentResponse(comment);
    }

    /**
     * Crée un nouveau commentaire
     * @param commentRequest DTO contenant les données du commentaire à créer
     * @param userId Identifiant de l'utilisateur créant le commentaire
     * @return DTO de réponse contenant les données du commentaire créé
     * @throws EntityNotFoundException si l'article ou l'utilisateur n'existe pas
     */
    @Transactional
    public CommentResponse createComment(CommentRequest commentRequest, Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id : " + userId));
        
        Article article = articleRepository.findById(commentRequest.getArticleId())
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé avec l'id : " + commentRequest.getArticleId()));
        
        Comment comment = new Comment();
        comment.setContent(commentRequest.getContent());
        comment.setAuthor(author);
        comment.setArticle(article);
        
        Comment savedComment = commentRepository.save(comment);
        return mapToCommentResponse(savedComment);
    }

    /**
     * Met à jour un commentaire existant
     * @param id Identifiant du commentaire à mettre à jour
     * @param commentRequest DTO contenant les données mises à jour
     * @param userId Identifiant de l'utilisateur effectuant la mise à jour
     * @return DTO de réponse contenant les données du commentaire mis à jour
     * @throws EntityNotFoundException si le commentaire, l'article ou l'utilisateur n'existe pas
     * @throws IllegalStateException si l'utilisateur n'est pas l'auteur du commentaire
     */
    @Transactional
    public CommentResponse updateComment(Long id, CommentRequest commentRequest, Long userId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commentaire non trouvé avec l'id : " + id));
        
        // Vérifier que l'utilisateur est bien l'auteur du commentaire
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("Seul l'auteur peut modifier ce commentaire");
        }
        
        // Vérifier si l'article a changé
        if (!comment.getArticle().getId().equals(commentRequest.getArticleId())) {
            Article article = articleRepository.findById(commentRequest.getArticleId())
                    .orElseThrow(() -> new EntityNotFoundException("Article non trouvé avec l'id : " + commentRequest.getArticleId()));
            comment.setArticle(article);
        }
        
        comment.setContent(commentRequest.getContent());
        
        Comment updatedComment = commentRepository.save(comment);
        return mapToCommentResponse(updatedComment);
    }

    /**
     * Supprime un commentaire
     * @param id Identifiant du commentaire à supprimer
     * @param userId Identifiant de l'utilisateur effectuant la suppression
     * @throws EntityNotFoundException si le commentaire n'existe pas
     * @throws IllegalStateException si l'utilisateur n'est pas l'auteur du commentaire
     */
    @Transactional
    public void deleteComment(Long id, Long userId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commentaire non trouvé avec l'id : " + id));
        
        // Vérifier que l'utilisateur est bien l'auteur du commentaire
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("Seul l'auteur peut supprimer ce commentaire");
        }
        
        commentRepository.delete(comment);
    }

    /**
     * Transforme une entité Comment en DTO de réponse
     * @param comment Entité Comment à transformer
     * @return DTO de réponse contenant les données du commentaire
     */
    private CommentResponse mapToCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .authorId(comment.getAuthor().getId())
                .authorUsername(comment.getAuthor().getUsername())
                .articleId(comment.getArticle().getId())
                .articleTitle(comment.getArticle().getTitle())
                .build();
    }
}
