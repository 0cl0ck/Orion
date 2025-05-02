import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { TokenStorageService } from '../../../services/token-storage.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  isLoginFailed = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private location: Location,
    private router: Router,
    private authService: AuthService,
    private tokenStorage: TokenStorageService
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    // Si l'utilisateur est déjà connecté, rediriger vers la page d'articles
    if (this.tokenStorage.getToken()) {
      this.router.navigate(['/articles']);
    }
  }

  /**
   * Retour à la page précédente
   */
  goBack(): void {
    this.location.back();
  }

  /**
   * Soumission du formulaire de connexion
   */
  onSubmit(): void {
    if (this.loginForm.valid) {
      const loginData = this.loginForm.value;
      console.log('Tentative de connexion avec:', loginData.email);
      
      this.authService.login(loginData).subscribe({
        next: data => {
          console.log('Réponse de connexion complète:', data);
          console.log('Token présent dans la réponse:', !!data.token);
          
          this.tokenStorage.saveToken(data.token);
          this.tokenStorage.saveUser(data);
          
          // Vérifier si le token est bien stocké
          const storedToken = this.tokenStorage.getToken();
          console.log('Token stocké après connexion:', !!storedToken);
          if (storedToken) {
            console.log('Début du token stocké:', storedToken.substring(0, 20) + '...');
          }
          
          this.isLoginFailed = false;
          this.router.navigate(['/articles']);
        },
        error: err => {
          console.error('Erreur de connexion:', err);
          console.error('Détails de l\'erreur:', err.error);
          this.errorMessage = err.error.message || 'Erreur de connexion';
          this.isLoginFailed = true;
        }
      });
    } else {
      // Marquer tous les champs comme touchés pour afficher les erreurs
      Object.keys(this.loginForm.controls).forEach(key => {
        const control = this.loginForm.get(key);
        control?.markAsTouched();
      });
    }
  }
}
