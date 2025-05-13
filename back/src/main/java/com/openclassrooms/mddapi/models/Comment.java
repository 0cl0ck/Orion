package com.openclassrooms.mddapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString(exclude = {"author", "article"})  // Évite les boucles infinies avec les relations
@EqualsAndHashCode(of = "id")  // Se base uniquement sur l'ID pour les comparaisons
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;
}
