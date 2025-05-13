package com.openclassrooms.mddapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validateur pour l'annotation @StrongPassword.
 * Vérifie que le mot de passe respecte les critères de complexité définis dans les spécifications.
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    // Regex pour vérifier la complexité du mot de passe
    // Au moins 8 caractères, une minuscule, une majuscule, un chiffre, un caractère spécial
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        // Pas d'initialisation nécessaire
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // Un mot de passe null est géré par @NotBlank
        if (password == null) {
            return true;
        }
        
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}
