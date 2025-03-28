package com.openclassrooms.mddapi.services;

import com.openclassrooms.mddapi.dto.ThemeRequest;
import com.openclassrooms.mddapi.dto.ThemeResponse;
import com.openclassrooms.mddapi.models.Theme;
import com.openclassrooms.mddapi.repositories.ThemeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ThemeService {
    
    @Autowired
    private ThemeRepository themeRepository;
    
    /**
     * Récupère tous les thèmes
     * @return Liste de tous les thèmes transformés en DTO de réponse
     */
    public List<ThemeResponse> getAllThemes() {
        return themeRepository.findAll()
                .stream()
                .map(this::mapToThemeResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère un thème par son identifiant
     * @param id Identifiant du thème
     * @return DTO de réponse contenant les données du thème
     * @throws EntityNotFoundException si le thème n'existe pas
     */
    public ThemeResponse getThemeById(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Thème non trouvé avec l'id : " + id));
        return mapToThemeResponse(theme);
    }
    
    /**
     * Récupère un thème par son nom
     * @param name Nom du thème
     * @return DTO de réponse contenant les données du thème
     * @throws EntityNotFoundException si le thème n'existe pas
     */
    public ThemeResponse getThemeByName(String name) {
        Theme theme = themeRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Thème non trouvé avec le nom : " + name));
        return mapToThemeResponse(theme);
    }
    
    /**
     * Crée un nouveau thème
     * @param themeRequest DTO contenant les données du thème à créer
     * @return DTO de réponse contenant les données du thème créé
     */
    @Transactional
    public ThemeResponse createTheme(ThemeRequest themeRequest) {
        Theme theme = new Theme();
        theme.setName(themeRequest.getName());
        theme.setDescription(themeRequest.getDescription());
        
        Theme savedTheme = themeRepository.save(theme);
        return mapToThemeResponse(savedTheme);
    }
    
    /**
     * Met à jour un thème existant
     * @param id Identifiant du thème à mettre à jour
     * @param themeRequest DTO contenant les données mises à jour
     * @return DTO de réponse contenant les données du thème mis à jour
     * @throws EntityNotFoundException si le thème n'existe pas
     */
    @Transactional
    public ThemeResponse updateTheme(Long id, ThemeRequest themeRequest) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Thème non trouvé avec l'id : " + id));
        
        theme.setName(themeRequest.getName());
        theme.setDescription(themeRequest.getDescription());
        
        Theme updatedTheme = themeRepository.save(theme);
        return mapToThemeResponse(updatedTheme);
    }
    
    /**
     * Supprime un thème
     * @param id Identifiant du thème à supprimer
     * @throws EntityNotFoundException si le thème n'existe pas
     */
    @Transactional
    public void deleteTheme(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Thème non trouvé avec l'id : " + id));
        
        themeRepository.delete(theme);
    }
    
    /**
     * Transforme une entité Theme en DTO de réponse
     * @param theme Entité Theme à transformer
     * @return DTO de réponse contenant les données du thème
     */
    private ThemeResponse mapToThemeResponse(Theme theme) {
        return ThemeResponse.builder()
                .id(theme.getId())
                .name(theme.getName())
                .description(theme.getDescription())
                .articleCount(theme.getArticles() != null ? theme.getArticles().size() : 0)
                .build();
    }
}