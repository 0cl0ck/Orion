package com.openclassrooms.mddapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation personnalisée pour valider qu'un mot de passe est suffisamment fort.
 * Un mot de passe fort doit contenir au moins 8 caractères, une minuscule, 
 * une majuscule, un chiffre et un caractère spécial.
 */
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "Le mot de passe doit contenir au moins 8 caractères, une minuscule, une majuscule, un chiffre et un caractère spécial";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
