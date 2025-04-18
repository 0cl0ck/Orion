import { Injectable } from '@angular/core';
import { Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { TokenStorageService } from './token-storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard {
  
  constructor(
    private tokenStorageService: TokenStorageService,
    private router: Router
  ) {}

  canActivate(): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    if (this.tokenStorageService.getToken()) {
      return true;
    }
    
    // Rediriger vers la page de connexion si l'utilisateur n'est pas authentifié
    this.router.navigate(['/login']);
    return false;
  }
}
