import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { TokenStorageService } from '../../../services/token-storage.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {
  registerForm: FormGroup;
  isSuccessful = false;
  isSignUpFailed = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private location: Location,
    private router: Router,
    private authService: AuthService,
    private tokenStorage: TokenStorageService
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/)
      ]]
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
   * Soumission du formulaire d'inscription
   */
  onSubmit(): void {
    if (this.registerForm.valid) {
      const userData = this.registerForm.value;
      
      this.authService.register(userData).subscribe({
        next: data => {
          this.isSuccessful = true;
          this.isSignUpFailed = false;
          
          // Rediriger vers la page de connexion après un délai de 2 secondes
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        },
        error: err => {
          this.errorMessage = err.error.message || 'Erreur lors de l\'inscription';
          this.isSignUpFailed = true;
        }
      });
    } else {
      // Marquer tous les champs comme touchés pour afficher les erreurs
      Object.keys(this.registerForm.controls).forEach(key => {
        const control = this.registerForm.get(key);
        control?.markAsTouched();
      });
    }
  }
}
