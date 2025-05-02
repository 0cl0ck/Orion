import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
  HTTP_INTERCEPTORS
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { TokenStorageService } from './token-storage.service';
import { NotificationService } from './notification.service';

/**
 * Intercepteur global pour gérer uniformément les erreurs HTTP
 * Il intercepte toutes les erreurs de requêtes HTTP et applique un traitement cohérent
 */
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor(
    private router: Router,
    private tokenService: TokenStorageService,
    private notificationService: NotificationService
  ) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Une erreur inattendue est survenue';
        
        // Log de l'erreur pour débogage
        console.error(`[ErrorInterceptor] Erreur HTTP ${error.status}:`, error);
        
        switch (error.status) {
          case 0: 
            // Erreur de connexion réseau / serveur inaccessible
            errorMessage = 'Impossible de se connecter au serveur. Veuillez vérifier votre connexion internet.';
            break;
            
          case 400:
            // Mauvaise requête - Erreur de validation
            errorMessage = 'Données invalides. Veuillez vérifier les informations saisies.';
            if (error.error?.message) {
              errorMessage = error.error.message;
            }
            break;
            
          case 401:
            // Non authentifié
            errorMessage = 'Session expirée. Veuillez vous reconnecter.';
            // Déconnexion automatique et redirection vers la page de login
            this.tokenService.signOut();
            this.router.navigate(['/login'], { 
              queryParams: { returnUrl: this.router.url, reason: 'session_expired' }
            });
            break;
            
          case 403:
            // Accès refusé
            errorMessage = 'Vous n\'avez pas les droits nécessaires pour effectuer cette action.';
            break;
            
          case 404:
            // Ressource non trouvée
            errorMessage = 'La ressource demandée n\'existe pas.';
            // Extraction du message personnalisé s'il existe
            if (error.error?.message) {
              errorMessage = error.error.message;
            }
            break;
            
          case 409:
            // Conflit
            errorMessage = 'Un conflit est survenu avec les données existantes.';
            break;
            
          case 500:
            // Erreur serveur
            errorMessage = 'Une erreur est survenue sur le serveur. Veuillez réessayer plus tard.';
            break;
            
          default:
            // Autres erreurs
            if (error.error?.message) {
              errorMessage = error.error.message;
            }
        }
        
        // Afficher la notification d'erreur à l'utilisateur
        this.notificationService.error(errorMessage);
        
        // En mode développement, conserver l'erreur dans une propriété pour faciliter le débogage
        if (typeof window !== 'undefined') {
          (window as any).lastHttpError = {
            status: error.status,
            message: errorMessage,
            originalError: error,
            timestamp: new Date()
          };
        }
        
        // Propagation de l'erreur avec un message standardisé
        return throwError(() => ({
          status: error.status,
          message: errorMessage,
          originalError: error
        }));
      })
    );
  }
}

export const errorInterceptorProviders = [
  { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }
];
