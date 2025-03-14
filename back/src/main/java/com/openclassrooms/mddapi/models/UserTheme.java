package com.openclassrooms.mddapi.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "user_theme")
public class UserTheme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;
}
