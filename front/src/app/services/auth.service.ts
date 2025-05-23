import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginRequest } from '../models/login-request.model';
import { RegisterRequest } from '../models/register-request.model';
import { environment } from '../../environments/environment';

const AUTH_API = `${environment.apiUrl}/api/auth/`;

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient) { }

  login(loginRequest: LoginRequest): Observable<any> {
    return this.http.post(AUTH_API + 'login', loginRequest, httpOptions);
  }

  register(registerRequest: RegisterRequest): Observable<any> {
    return this.http.post(AUTH_API + 'register', registerRequest, httpOptions);
  }
  
  /**
   * Rafraîchit le token après modification du profil
   * 
   * @param email Email de l'utilisateur (nouveau ou actuel)
   * @param password Mot de passe (actuel ou nouveau si modifié)
   * @returns Observable contenant le nouveau token et les informations utilisateur
   */
  refreshToken(email: string, password: string): Observable<any> {
    console.log('Demande de rafraîchissement du token pour:', email);
    const loginRequest: LoginRequest = {
      email: email,
      password: password
    };
    return this.http.post(AUTH_API + 'login', loginRequest, httpOptions);
  }
}
