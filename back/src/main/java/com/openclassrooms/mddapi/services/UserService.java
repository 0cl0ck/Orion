package com.openclassrooms.mddapi.services;

import com.openclassrooms.mddapi.dto.UserRequest;
import com.openclassrooms.mddapi.dto.UserResponse;
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
import java.util.stream.Collectors;

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
     * @return Liste de tous les utilisateurs transformés en DTO de réponse
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un utilisateur par son ID
     * @param id Identifiant de l'utilisateur
     * @return DTO de réponse contenant les données de l'utilisateur
     * @throws EntityNotFoundException si l'utilisateur n'existe pas
     */
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id : " + id));
        return mapToUserResponse(user);
    }

    /**
     * Récupère un utilisateur par son nom d'utilisateur
     * @param username Nom d'utilisateur
     * @return DTO de réponse contenant les données de l'utilisateur
     * @throws EntityNotFoundException si l'utilisateur n'existe pas
     */
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec le nom d'utilisateur : " + username));
        return mapToUserResponse(user);
    }

    /**
     * Récupère un utilisateur par son email
     * @param email Email de l'utilisateur
     * @return DTO de réponse contenant les données de l'utilisateur
     * @throws EntityNotFoundException si l'utilisateur n'existe pas
     */
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'email : " + email));
        return mapToUserResponse(user);
    }

    /**
     * Met à jour les informations d'un utilisateur
     * @param id Identifiant de l'utilisateur à mettre à jour
     * @param userRequest DTO contenant les données mises à jour
     * @param currentUserId Identifiant de l'utilisateur effectuant la mise à jour
     * @return DTO de réponse contenant les données de l'utilisateur mis à jour
     * @throws EntityNotFoundException si l'utilisateur n'existe pas
     * @throws AccessDeniedException si l'utilisateur essaie de modifier un autre utilisateur
     */
    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest, Long currentUserId) {
        System.out.println("Tentative de mise à jour de l'utilisateur - ID: " + id + ", currentUserId: " + currentUserId);
        System.out.println("Données reçues: username=" + userRequest.getUsername() + ", email=" + userRequest.getEmail() + ", password présent: " + (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()));
        
        try {
            // Vérifier si l'utilisateur essaie de modifier un autre compte que le sien
            if (!id.equals(currentUserId)) {
                System.out.println("Erreur: Tentative de modification d'un autre compte");
                throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier cet utilisateur");
            }

            System.out.println("Recherche de l'utilisateur dans la base de données");
            User user = userRepository.findById(id)
                    .orElseThrow(() -> {
                        System.out.println("Erreur: Utilisateur non trouvé avec l'id: " + id);
                        return new EntityNotFoundException("Utilisateur non trouvé avec l'id : " + id);
                    });
            System.out.println("Utilisateur trouvé: " + user.getUsername() + " (" + user.getEmail() + ")");

            // Mettre à jour les champs modifiables
            if (userRequest.getUsername() != null && !userRequest.getUsername().isEmpty()) {
                System.out.println("Vérification si le nom d'utilisateur " + userRequest.getUsername() + " existe déjà");
                // Vérifier si le nouveau nom d'utilisateur existe déjà (sauf s'il s'agit du même utilisateur)
                Optional<User> existingUser = userRepository.findByUsername(userRequest.getUsername());
                if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                    System.out.println("Erreur: Nom d'utilisateur déjà utilisé par un autre utilisateur");
                    throw new IllegalArgumentException("Ce nom d'utilisateur est déjà utilisé");
                }
                System.out.println("Mise à jour du nom d'utilisateur: " + user.getUsername() + " -> " + userRequest.getUsername());
                user.setUsername(userRequest.getUsername());
            }

            if (userRequest.getEmail() != null && !userRequest.getEmail().isEmpty()) {
                System.out.println("Vérification si l'email " + userRequest.getEmail() + " existe déjà");
                // Vérifier si le nouvel email existe déjà (sauf s'il s'agit du même utilisateur)
                Optional<User> existingUser = userRepository.findByEmail(userRequest.getEmail());
                if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                    System.out.println("Erreur: Email déjà utilisé par un autre utilisateur");
                    throw new IllegalArgumentException("Cet email est déjà utilisé");
                }
                System.out.println("Mise à jour de l'email: " + user.getEmail() + " -> " + userRequest.getEmail());
                user.setEmail(userRequest.getEmail());
            }

            // Si un nouveau mot de passe est fourni, le hasher
            if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
                System.out.println("Mise à jour du mot de passe");
                user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
            }

            user.setUpdatedAt(LocalDateTime.now());
            
            System.out.println("Sauvegarde des modifications dans la base de données");
            User updatedUser = userRepository.save(user);
            System.out.println("Utilisateur mis à jour avec succès");
            return mapToUserResponse(updatedUser);
        } catch (Exception e) {
            System.out.println("Une exception s'est produite lors de la mise à jour du profil: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw e; // Relancer l'exception pour la gestion standard
        }
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
    
    /**
     * Vérifie si un utilisateur existe par son ID
     * @param id Identifiant de l'utilisateur à vérifier
     * @return true si l'utilisateur existe, false sinon
     */
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
    
    /**
     * Transforme une entité User en DTO de réponse
     * @param user Entité User à transformer
     * @return DTO de réponse contenant les données de l'utilisateur
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .articleCount(user.getArticles() != null ? user.getArticles().size() : 0)
                .commentCount(user.getComments() != null ? user.getComments().size() : 0)
                .build();
    }
}
