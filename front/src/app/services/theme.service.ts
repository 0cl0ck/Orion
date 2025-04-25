import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Theme } from '../models/theme.model';
import { environment } from '../../environments/environment';

const API_URL = `${environment.apiUrl}/api/themes`;

@Injectable({
  providedIn: 'root'
})
export class ThemeService {

  constructor(private http: HttpClient) { }

  /**
   * Récupère tous les thèmes
   */
  getAllThemes(): Observable<Theme[]> {
    // TODO: Récupérer les thèmes depuis l'API et inclure les abonnements de l'utilisateur
    // Pour le moment, retournons des données mockées en attendant que le backend soit prêt
    const mockThemes: Theme[] = [
      { id: 1, name: 'Développement Web', description: 'Tout sur le développement web frontend et backend, les frameworks et les bonnes pratiques.', isSubscribed: false },
      { id: 2, name: 'Intelligence Artificielle', description: 'Actualités et discussions sur l\'IA, le Machine Learning, les LLMs et leurs applications.', isSubscribed: true },
      { id: 3, name: 'Cybersécurité', description: 'Sécurité informatique, protection des données, cryptographie et prévention des attaques.', isSubscribed: false },
      { id: 4, name: 'DevOps', description: 'Intégration continue, déploiement continu, Docker, Kubernetes et infrastructures cloud.', isSubscribed: false },
      { id: 5, name: 'Mobile', description: 'Développement d\'applications mobiles sur iOS, Android et solutions cross-platform.', isSubscribed: true },
      { id: 6, name: 'Big Data', description: 'Analyse et traitement de grandes quantités de données, technologies et outils associés.', isSubscribed: false }
    ];
    
    return of(mockThemes);
    // Quand l'API sera prête, utiliser :
    // return this.http.get<Theme[]>(API_URL);
  }

  /**
   * Récupère un thème par son ID
   * @param id L'identifiant du thème
   */
  getThemeById(id: number): Observable<Theme> {
    return this.http.get<Theme>(`${API_URL}/${id}`);
  }
  
  /**
   * S'abonner à un thème
   * @param themeId L'identifiant du thème
   */
  subscribeToTheme(themeId: number): Observable<any> {
    // Quand l'API sera prête :
    // return this.http.post(`${API_URL}/${themeId}/subscribe`, {});
    
    // En attendant, on simule une réponse positive
    return of({ success: true, message: 'Abonnement réussi' });
  }
  
  /**
   * Se désabonner d'un thème
   * @param themeId L'identifiant du thème
   */
  unsubscribeFromTheme(themeId: number): Observable<any> {
    // Quand l'API sera prête :
    // return this.http.post(`${API_URL}/${themeId}/unsubscribe`, {});
    
    // En attendant, on simule une réponse positive
    return of({ success: true, message: 'Désabonnement réussi' });
  }

  /**
   * Récupère les articles d'un thème spécifique
   * @param themeId L'identifiant du thème
   */
  getThemeArticles(themeId: number): Observable<any> {
    return this.http.get(`${API_URL}/${themeId}/articles`);
  }
}
