import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Article } from '../models/article.model';
import { environment } from '../../environments/environment';

const API_URL = `${environment.apiUrl}/api/articles`;

@Injectable({
  providedIn: 'root'
})
export class ArticleService {

  constructor(private http: HttpClient) { }

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
    return this.http.post<Article>(API_URL, article);
  }
}
