package com.openclassrooms.mddapi.services;

import com.openclassrooms.mddapi.models.User;
import com.openclassrooms.mddapi.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des utilisateurs
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Récupère tous les utilisateurs
     * @return Liste de tous les utilisateurs
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Récupère un utilisateur par son ID
     * @param id Identifiant de l'utilisateur
     * @return Utilisateur trouvé
     * @throws EntityNotFoundException si l'utilisateur n'existe pas
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id : " + id));
    }

    /**
     * Récupère un utilisateur par son nom d'utilisateur
     * @param username Nom d'utilisateur
     * @return Utilisateur trouvé
     * @throws EntityNotFoundException si l'utilisateur n'existe pas
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec le nom d'utilisateur : " + username));
    }

    /**
     * Récupère un utilisateur par son email
     * @param email Email de l'utilisateur
     * @return Utilisateur trouvé
     * @throws EntityNotFoundException si l'utilisateur n'existe pas
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'email : " + email));
    }

    /**
     * Met à jour les informations d'un utilisateur
     * @param id Identifiant de l'utilisateur à mettre à jour
     * @param userData Données mises à jour de l'utilisateur
     * @param currentUserId Identifiant de l'utilisateur effectuant la mise à jour
     * @return Utilisateur mis à jour
     * @throws EntityNotFoundException si l'utilisateur n'existe pas
     * @throws AccessDeniedException si l'utilisateur essaie de modifier un autre utilisateur
     */
    @Transactional
    public User updateUser(Long id, User userData, Long currentUserId) {
        // Vérifier si l'utilisateur essaie de modifier un autre compte que le sien
        if (!id.equals(currentUserId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier cet utilisateur");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id : " + id));

        // Mettre à jour les champs modifiables
        if (userData.getUsername() != null && !userData.getUsername().isEmpty()) {
            // Vérifier si le nouveau nom d'utilisateur existe déjà (sauf s'il s'agit du même utilisateur)
            Optional<User> existingUser = userRepository.findByUsername(userData.getUsername());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                throw new IllegalArgumentException("Ce nom d'utilisateur est déjà utilisé");
            }
            user.setUsername(userData.getUsername());
        }

        if (userData.getEmail() != null && !userData.getEmail().isEmpty()) {
            // Vérifier si le nouvel email existe déjà (sauf s'il s'agit du même utilisateur)
            Optional<User> existingUser = userRepository.findByEmail(userData.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                throw new IllegalArgumentException("Cet email est déjà utilisé");
            }
            user.setEmail(userData.getEmail());
        }

        // Si un nouveau mot de passe est fourni, le hasher
        if (userData.getPassword() != null && !userData.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userData.getPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    /**
     * Supprime un utilisateur
     * @param id Identifiant de l'utilisateur à supprimer
     * @param currentUserId Identifiant de l'utilisateur effectuant la suppression
     * @throws EntityNotFoundException si l'utilisateur n'existe pas
     * @throws AccessDeniedException si l'utilisateur essaie de supprimer un autre utilisateur
     */
    @Transactional
    public void deleteUser(Long id, Long currentUserId) {
        // Vérifier si l'utilisateur essaie de supprimer un autre compte que le sien
        if (!id.equals(currentUserId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à supprimer cet utilisateur");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id : " + id));

        userRepository.delete(user);
    }

    /**
     * Vérifie si un email existe déjà
     * @param email Email à vérifier
     * @return true si l'email existe, false sinon
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Vérifie si un nom d'utilisateur existe déjà
     * @param username Nom d'utilisateur à vérifier
     * @return true si le nom d'utilisateur existe, false sinon
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
