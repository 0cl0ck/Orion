package com.openclassrooms.mddapi.security.jwt;

import com.openclassrooms.mddapi.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Classe utilitaire pour la gestion des tokens JWT (JSON Web Token).
 * 
 * Cette classe fournit des méthodes pour générer, valider et extraire des informations
 * des tokens JWT utilisés pour l'authentification et l'autorisation dans l'application.
 * 
 * Les tokens JWT contiennent l'ID utilisateur comme sujet et le nom d'utilisateur comme claim.
 * Ils sont signés avec un secret configuré dans les propriétés de l'application et ont
 * une durée de validité configurable.
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Génère un token JWT à partir des informations d'authentification.
     * 
     * @param authentication L'objet Authentication contenant les détails de l'utilisateur connecté
     * @return Un token JWT signé contenant l'ID utilisateur comme sujet et le nom d'utilisateur comme claim
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        
        return Jwts.builder()
                .setSubject(userPrincipal.getId().toString()) // Utilisation de l'ID comme sujet (stable)
                .claim("username", userPrincipal.getUsername()) // Ajouter le nom d'utilisateur comme une revendication
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Crée une clé HMAC-SHA à partir du secret JWT configuré.
     * 
     * @return La clé utilisée pour signer les tokens JWT
     */
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Extrait l'ID utilisateur (sujet) du token JWT
     * @param token Le token JWT
     * @return L'identifiant utilisateur
     */
    public Long getUserIdFromJwtToken(String token) {
        String subject = Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
        return Long.parseLong(subject);
    }
    
    /**
     * Extrait le nom d'utilisateur du token JWT (à partir de la revendication username)
     * Cette méthode est maintenue pour la compatibilité avec le code existant
     * @param token Le token JWT
     * @return Le nom d'utilisateur
     */
    public String getUserNameFromJwtToken(String token) {
        // Récupérer l'attribut username à partir des claims
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().get("username", String.class);
    }

    /**
     * Valide un token JWT.
     * 
     * @param authToken Le token JWT à valider
     * @return true si le token est valide, false sinon
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.warn("Token JWT invalide - format incorrect");
        } catch (ExpiredJwtException e) {
            logger.warn("Token JWT expiré");
        } catch (UnsupportedJwtException e) {
            logger.warn("Token JWT non supporté");
        } catch (IllegalArgumentException e) {
            logger.warn("La chaîne de revendications JWT est vide");
        }

        return false;
    }
} 