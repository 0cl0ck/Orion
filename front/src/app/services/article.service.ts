import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Article, Comment } from '../models/article.model';
import { TokenStorageService } from './token-storage.service';

const API_URL = `${environment.apiUrl}/api/articles`;

@Injectable({
  providedIn: 'root'
})
export class ArticleService {

  constructor(
    private http: HttpClient,
    private tokenService: TokenStorageService
  ) { }

  /**
   * Récupère tous les articles
   */
  getAllArticles(): Observable<Article[]> {
    return this.http.get<Article[]>(API_URL);
  }

  /**
   * Récupère un article par son ID
   * @param id L'identifiant de l'article
   */
  getArticleById(id: number): Observable<Article> {
    return this.http.get<Article>(`${API_URL}/${id}`);
  }

  /**
   * Récupère les articles d'un thème spécifique
   * @param themeId L'identifiant du thème
   */
  getArticlesByTheme(themeId: number): Observable<Article[]> {
    return this.http.get<Article[]>(`${API_URL}/theme/${themeId}`);
  }

  /**
   * Récupère les articles d'un utilisateur spécifique
   * @param userId L'identifiant de l'utilisateur
   */
  getArticlesByUser(userId: number): Observable<Article[]> {
    return this.http.get<Article[]>(`${API_URL}/user/${userId}`);
  }

  /**
   * Recherche des articles par titre
   * @param title Le titre à rechercher
   */
  searchArticlesByTitle(title: string): Observable<Article[]> {
    return this.http.get<Article[]>(`${API_URL}/search?title=${title}`);
  }

  /**
   * Crée un nouvel article
   * @param article Les données de l'article à créer
   */
  createArticle(article: any): Observable<Article> {
    console.log('Debug article à créer:', article);
    
    // S'assurer que themeId est un nombre (le backend attend un Long)
    const themeId = typeof article.themeId === 'string' ? parseInt(article.themeId, 10) : article.themeId;
    
    // Format simple comme pour les commentaires qui fonctionnent
    return this.http.post<Article>(`${environment.apiUrl}/api/articles`, {
      title: article.title,
      content: article.content,
      themeId: themeId
    }).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Une erreur est survenue lors de la création de l\'article.';
        
        console.error(`Erreur HTTP ${error.status} lors de la création de l'article:`, error);
        console.error('Données envoyées au serveur:', { title: article.title, content: article.content?.substring(0, 30) + '...', themeId });
        
        // Analyse précise par code d'erreur
        switch (error.status) {
          case 401:
            // Problème d'authentification
            console.error('Erreur 401: Problème d\'authentification - Token invalide ou expiré');
            errorMessage = 'Vous n\'\u00eates pas authentifié ou votre session a expiré. Veuillez vous reconnecter.';
            // Vérifier l'état du token
            const token = this.tokenService.getToken();
            console.error('Token présent:', !!token);
            break;
            
          case 404:
            // Ressource introuvable (probablement le thème)
            console.error('Erreur 404: Ressource introuvable - Le thème sélectionné n\'existe pas dans la base de données');
            errorMessage = 'Le thème sélectionné n\'existe pas dans la base de données. Veuillez sélectionner un thème valide.';
            break;
            
          case 400:
            // Données invalides
            console.error('Erreur 400: Données invalides - Problème de validation des données');
            errorMessage = 'Les données de l\'article sont invalides. Veuillez vérifier et réessayer.';
            break;
            
          case 500:
            // Erreur serveur
            console.error('Erreur 500: Erreur interne du serveur');
            errorMessage = 'Une erreur est survenue sur le serveur. Veuillez réessayer plus tard.';
            break;
            
          default:
            console.error(`Erreur inattendue (${error.status}) lors de la création de l'article:`, error);
            errorMessage = `Une erreur inattendue est survenue (${error.status}). Veuillez réessayer plus tard.`;
        }
        
        return throwError(() => ({ status: error.status, message: errorMessage }));
      })
    );
  }

  /**
   * Ajoute un commentaire à un article
   * @param comment Les données du commentaire (articleId et content)
   */
  addComment(comment: { articleId: number, content: string }): Observable<any> {
    return this.http.post(`${environment.apiUrl}/api/comments`, { 
      articleId: comment.articleId,
      content: comment.content 
    });
  }



  /**
   * Récupère les commentaires d'un article spécifique
   * @param articleId L'identifiant de l'article
   */
  getCommentsByArticle(articleId: number): Observable<Comment[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/api/comments/article/${articleId}`)
      .pipe(
        map(comments => comments.map(comment => ({
          id: comment.id,
          content: comment.content,
          authorId: comment.author?.id,
          authorUsername: comment.author?.username || 'Utilisateur inconnu',
          createdAt: comment.createdAt
        })))
      );
  }
}
