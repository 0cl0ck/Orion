package com.openclassrooms.mddapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.util.List;

@Entity
@Table(name = "themes")
@Getter
@Setter
@ToString(exclude = {"articles", "userThemes"})  // Évite les boucles infinies avec les relations
@EqualsAndHashCode(of = "id")  // Se base uniquement sur l'ID pour les comparaisons
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "theme", cascade = CascadeType.ALL)
    private List<Article> articles;
    
    @OneToMany(mappedBy = "theme", cascade = CascadeType.ALL)
    private List<UserTheme> userThemes;
}
