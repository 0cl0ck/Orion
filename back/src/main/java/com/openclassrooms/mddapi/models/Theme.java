package com.openclassrooms.mddapi.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "themes")
@Data
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
