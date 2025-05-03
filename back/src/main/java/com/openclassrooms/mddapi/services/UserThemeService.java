package com.openclassrooms.mddapi.services;

import com.openclassrooms.mddapi.models.Theme;
import com.openclassrooms.mddapi.models.User;
import com.openclassrooms.mddapi.models.UserTheme;
import com.openclassrooms.mddapi.repositories.ThemeRepository;
import com.openclassrooms.mddapi.repositories.UserRepository;
import com.openclassrooms.mddapi.repositories.UserThemeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserThemeService {

    @Autowired
    private UserThemeRepository userThemeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ThemeRepository themeRepository;

    /**
     * Vérifie si un utilisateur est abonné à un thème
     * @param userId ID de l'utilisateur
     * @param themeId ID du thème
     * @return true si l'utilisateur est abonné au thème, false sinon
     */
    public boolean isUserSubscribedToTheme(Long userId, Long themeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id : " + userId));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new EntityNotFoundException("Thème non trouvé avec l'id : " + themeId));

        return userThemeRepository.findByUserAndTheme(user, theme).isPresent();
    }

    /**
     * Abonne un utilisateur à un thème
     * @param userId ID de l'utilisateur
     * @param themeId ID du thème
     * @return true si l'abonnement a été créé, false si l'utilisateur était déjà abonné
     */
    @Transactional
    public boolean subscribeUserToTheme(Long userId, Long themeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id : " + userId));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new EntityNotFoundException("Thème non trouvé avec l'id : " + themeId));

        // Vérifier si l'abonnement existe déjà
        if (userThemeRepository.findByUserAndTheme(user, theme).isPresent()) {
            return false;  // L'utilisateur est déjà abonné
        }

        // Créer un nouvel abonnement
        UserTheme userTheme = new UserTheme();
        userTheme.setUser(user);
        userTheme.setTheme(theme);
        userThemeRepository.save(userTheme);
        return true;
    }

    /**
     * Désabonne un utilisateur d'un thème
     * @param userId ID de l'utilisateur
     * @param themeId ID du thème
     * @return true si le désabonnement a réussi, false si l'utilisateur n'était pas abonné
     */
    @Transactional
    public boolean unsubscribeUserFromTheme(Long userId, Long themeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id : " + userId));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new EntityNotFoundException("Thème non trouvé avec l'id : " + themeId));

        // Vérifier si l'abonnement existe
        if (userThemeRepository.findByUserAndTheme(user, theme).isEmpty()) {
            return false;  // L'utilisateur n'est pas abonné
        }

        // Supprimer l'abonnement
        userThemeRepository.deleteByUserAndTheme(user, theme);
        return true;
    }

    /**
     * Récupère tous les thèmes auxquels un utilisateur est abonné
     * @param userId ID de l'utilisateur
     * @return Liste des IDs des thèmes auxquels l'utilisateur est abonné
     */
    public List<Long> getSubscribedThemeIds(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id : " + userId));

        return userThemeRepository.findByUser(user).stream()
                .map(userTheme -> userTheme.getTheme().getId())
                .collect(Collectors.toList());
    }
}
