import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

const API_URL = `${environment.apiUrl}/api/users/`;

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) { }

  /**
   * Récupère les informations de l'utilisateur actuellement connecté
   */
  getCurrentUser(): Observable<any> {
    return this.http.get(API_URL + 'me');
  }

  /**
   * Met à jour les informations d'un utilisateur
   * @param id Identifiant de l'utilisateur
   * @param userData Données de l'utilisateur à mettre à jour
   */
  updateUser(id: number, userData: any): Observable<any> {
    return this.http.put(API_URL + id, userData);
  }

  /**
   * Vérifie si un nom d'utilisateur existe déjà
   * @param username Nom d'utilisateur à vérifier
   */
  checkUsernameExists(username: string): Observable<boolean> {
    return this.http.get<boolean>(API_URL + 'check/username/' + username);
  }

  /**
   * Vérifie si un email existe déjà
   * @param email Email à vérifier
   */
  checkEmailExists(email: string): Observable<boolean> {
    return this.http.get<boolean>(API_URL + 'check/email/' + email);
  }
}