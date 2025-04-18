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
}
