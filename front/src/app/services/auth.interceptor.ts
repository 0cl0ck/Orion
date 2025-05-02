import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HTTP_INTERCEPTORS
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { TokenStorageService } from './token-storage.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private tokenService: TokenStorageService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Obtenir le token
    const token = this.tokenService.getToken();
    
    // Cloner avec les headers d'authentification si un token est présent
    let authReq = request;
    if (token != null) {
      // HttpHeaders est immuable - créer une nouvelle instance avec tous les headers nécessaires
      const headers = request.headers
        .set('Authorization', 'Bearer ' + token)
        .set('Content-Type', 'application/json')
        .set('Accept', 'application/json');
      
      // Cloner la requête avec tous les headers nécessaires
      authReq = request.clone({ headers });
    }
    
    return next.handle(authReq);
  }
}

export const authInterceptorProviders = [
  { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
];
