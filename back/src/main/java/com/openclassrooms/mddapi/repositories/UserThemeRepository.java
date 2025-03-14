package com.openclassrooms.mddapi.repositories;

import com.openclassrooms.mddapi.models.Theme;
import com.openclassrooms.mddapi.models.User;
import com.openclassrooms.mddapi.models.UserTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserThemeRepository extends JpaRepository<UserTheme, Long> {
    List<UserTheme> findByUser(User user);
    List<UserTheme> findByTheme(Theme theme);
    Optional<UserTheme> findByUserAndTheme(User user, Theme theme);
    void deleteByUserAndTheme(User user, Theme theme);
} 