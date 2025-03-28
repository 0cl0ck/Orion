package com.openclassrooms.mddapi.controllers;

import com.openclassrooms.mddapi.dto.ThemeRequest;
import com.openclassrooms.mddapi.dto.ThemeResponse;
import com.openclassrooms.mddapi.services.ThemeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/themes")
public class ThemeController {

    @Autowired
    private ThemeService themeService;

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAllThemes() {
        List<ThemeResponse> themes = themeService.getAllThemes();
        return new ResponseEntity<>(themes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThemeResponse> getThemeById(@PathVariable Long id) {
        try {
            ThemeResponse theme = themeService.getThemeById(id);
            return new ResponseEntity<>(theme, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Thème non trouvé avec l'id: " + id, e);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ThemeResponse> createTheme(@Valid @RequestBody ThemeRequest themeRequest) {
        try {
            ThemeResponse createdTheme = themeService.createTheme(themeRequest);
            return new ResponseEntity<>(createdTheme, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erreur lors de la création du thème: " + e.getMessage(), e);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ThemeResponse> updateTheme(@PathVariable Long id, @Valid @RequestBody ThemeRequest themeRequest) {
        try {
            ThemeResponse updatedTheme = themeService.updateTheme(id, themeRequest);
            return new ResponseEntity<>(updatedTheme, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    e.getClass().getSimpleName().equals("EntityNotFoundException") ? 
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST, 
                    e.getMessage(), e
            );
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        try {
            themeService.deleteTheme(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    e.getClass().getSimpleName().equals("EntityNotFoundException") ? 
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST, 
                    e.getMessage(), e
            );
        }
    }
}