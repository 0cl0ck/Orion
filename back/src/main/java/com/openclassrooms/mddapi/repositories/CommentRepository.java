package com.openclassrooms.mddapi.repositories;

import com.openclassrooms.mddapi.models.Article;
import com.openclassrooms.mddapi.models.Comment;
import com.openclassrooms.mddapi.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticle(Article article);
    List<Comment> findByAuthor(User author);
    
    @Query("SELECT c FROM Comment c WHERE c.article.id = :articleId ORDER BY c.createdAt DESC")
    List<Comment> findByArticleIdOrderByCreatedAtDesc(Long articleId);
} 