# Orion - Réseau Social pour Développeurs (MDD)

Orion est une application full-stack développée avec Angular et Spring Boot qui permet aux développeurs de partager des articles, d'échanger sur différents thèmes techniques et de construire une communauté professionnelle.

## Aperçu du Projet

Cette application, nommée MDD (Monde de Dév), est un réseau social dédié aux développeurs qui vise à faciliter le partage de connaissances et la mise en relation entre professionnels du développement. Le MVP (Minimum Viable Product) comprend les fonctionnalités suivantes :

- Système d'authentification sécurisé (inscription/connexion avec JWT)
- Création et publication d'articles techniques
- Abonnement et exploration d'articles par thèmes
- Gestion de profil utilisateur
- Commentaires sur les articles
- Interface utilisateur moderne et responsive

## Structure du Projet

```
/
├── front/               # Application frontend Angular
└── back/                # API backend Spring Boot
```

## Technologies Utilisées

### Frontend
- Angular 14
- Angular Material pour l'interface utilisateur
- TypeScript
- SCSS pour les styles
- RxJS pour la programmation réactive
- Reactive Forms pour la gestion des formulaires

### Backend
- Spring Boot 3.2.3
- Spring Security avec JWT pour l'authentification
- Spring Data JPA pour la persistance des données
- Java 21
- Lombok pour la réduction du code boilerplate
- Spring Boot Actuator pour le monitoring
- MySQL 8.0+ comme base de données

## Installation et Démarrage

### Prérequis
- Node.js et npm
- JDK 21 (recommandé) ou JDK 17 minimum
- Maven
- MySQL 8.0+

### Frontend

```bash
# Naviguer vers le dossier frontend
cd front

# Installer les dépendances
npm install

# Démarrer le serveur de développement
ng serve
```

L'application sera accessible à l'adresse `http://localhost:4200/`

### Backend

```bash
# Naviguer vers le dossier backend
cd back

# Installer les dépendances et construire le projet
mvn clean install

# Exécuter l'application
mvn spring-boot:run
```

Alternativement, vous pouvez utiliser le script batch pour Java 21 :
```bash
./run-java21.bat
```

L'API sera accessible à l'adresse `http://localhost:8080/`

### Configuration MySQL

1. Démarrer le service MySQL :
```bash
mysql -u root -p
```

2. Créer une base de données nommée "mdd_db" :
```sql
CREATE DATABASE mdd_db;
```

3. Configurer les paramètres de connexion :
   - Copier le fichier `application.properties.example` vers `application.properties` dans le dossier `back/src/main/resources/`
   - Modifier les paramètres suivants :
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mdd_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=votre_nom_utilisateur
spring.datasource.password=votre_mot_de_passe
```

4. Structure de la base de données :
   - La base contient 5 tables principales : `users`, `articles`, `comments`, `themes`, et `user_theme`
   - Le schéma de base de données est automatiquement créé/mis à jour par Hibernate (`spring.jpa.hibernate.ddl-auto=update`)

> Note : Assurez-vous que l'utilisateur MySQL configuré possède les privilèges nécessaires pour créer/modifier les tables dans la base de données.

## Modèles Principaux

### Utilisateur (User)
- Gestion de profil
- Publication d'articles
- Commentaires sur les articles

### Article
- Création, modification et suppression d'articles
- Association à des thèmes

### Thème
- Catégorisation des articles
- Navigation par thème

## Routes et Endpoints Principaux

### Frontend
- `/` - Page d'accueil
- `/login` et `/register` - Authentification
- `/articles` - Liste des articles
- `/articles/:id` - Détail d'un article
- `/create-article` - Création d'article
- `/themes` - Liste des thèmes
- `/profile` - Profil utilisateur

### Backend
- `/api/auth/*` - Endpoints d'authentification
- `/api/articles/*` - Gestion des articles
- `/api/themes/*` - Gestion des thèmes
- `/api/users/*` - Gestion des utilisateurs

## Architecture et Sécurité

### Architecture en Couches

L'application backend suit une architecture en couches classique :

- **Couche Controller** : Points d'entrée de l'API REST, gestion des requêtes HTTP
- **Couche Service** : Logique métier et orchestration des opérations
- **Couche Repository** : Accès aux données et persistance
- **Couche Model** : Entités JPA et objets de transfert de données (DTO)

Cette séparation des responsabilités permet une meilleure maintenabilité et testabilité du code.

### Sécurité

L'application implémente plusieurs niveaux de sécurité :

- **Authentification JWT** : Tokens JWT pour l'authentification stateless
- **Autorisation basée sur les rôles** : Contrôle d'accès aux ressources selon le rôle utilisateur
- **Validation des données** : Validation des entrées utilisateur côté serveur et client
- **Headers de sécurité HTTP** : Protection contre les attaques web courantes
  - Content-Security-Policy
  - X-Frame-Options
  - Cache-Control
- **Hachage des mots de passe** : Utilisation de BCrypt pour le stockage sécurisé
- **Annotations @PreAuthorize** : Sécurité au niveau des méthodes

### Monitoring avec Spring Boot Actuator

Spring Boot Actuator est configuré pour fournir des informations sur l'état de l'application :

- `/actuator/health` : État de santé de l'application (accessible publiquement)
- `/actuator/info` : Informations sur l'application (accessible publiquement)
- `/actuator/metrics` : Métriques de l'application (accès restreint aux administrateurs)
- `/actuator/env` : Variables d'environnement (accès restreint aux administrateurs)

Ces endpoints permettent de surveiller l'application en production et de diagnostiquer rapidement les problèmes.