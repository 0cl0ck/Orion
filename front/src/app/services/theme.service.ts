import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
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
    return this.http.get<Theme[]>(API_URL);
  }

  /**
   * Récupère un thème par son ID
   * @param id L'identifiant du thème
   */
  getThemeById(id: number): Observable<Theme> {
    return this.http.get<Theme>(`${API_URL}/${id}`);
  }
}
