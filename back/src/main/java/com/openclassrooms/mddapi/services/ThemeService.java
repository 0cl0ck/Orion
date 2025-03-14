package com.openclassrooms.mddapi.services;

import com.openclassrooms.mddapi.models.Theme;
import com.openclassrooms.mddapi.repositories.ThemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ThemeService {
    
    @Autowired
    private ThemeRepository themeRepository;
    
    public List<Theme> getAllThemes() {
        return themeRepository.findAll();
    }
    
    public Optional<Theme> getThemeById(Long id) {
        return themeRepository.findById(id);
    }
    
    public Optional<Theme> getThemeByName(String name) {
        return themeRepository.findByName(name);
    }
    
    public Theme createTheme(Theme theme) {
        return themeRepository.save(theme);
    }
    
    public Theme updateTheme(Long id, Theme themeDetails) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thème non trouvé avec l'id : " + id));
        
        theme.setName(themeDetails.getName());
        theme.setDescription(themeDetails.getDescription());
        
        return themeRepository.save(theme);
    }
    
    public void deleteTheme(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thème non trouvé avec l'id : " + id));
        
        themeRepository.delete(theme);
    }
} 