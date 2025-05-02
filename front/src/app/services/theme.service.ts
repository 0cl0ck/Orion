import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
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
    return this.http.get<Theme[]>(`${environment.apiUrl}/api/themes`)
      .pipe(
        map(themes => {
          // Si l'API ne retourne pas la propriété isSubscribed, l'ajouter
          // avec une valeur par défaut (false)
          return themes.map(theme => ({
            ...theme,
            isSubscribed: theme.hasOwnProperty('isSubscribed') ? theme.isSubscribed : false
          }));
        }),
        catchError(error => {
          // En cas d'erreur, utiliser des données de secours qui correspondent aux thèmes réels de la base de données
          const fallbackThemes: Theme[] = [
            { id: 3, name: 'Tech & Innovation', description: 'Articles sur les dernières avancées technologiques et innovations', isSubscribed: false },
            { id: 4, name: 'Intelligence Artificielle', description: 'Articles sur l\'IA, le machine learning et leurs applications dans divers domaines', isSubscribed: false },
            { id: 5, name: 'Développement Web', description: 'Tout sur le développement web, frontend et backend, frameworks et bonnes pratiques', isSubscribed: false }
          ];
          
          return of(fallbackThemes);
        })
      );
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
