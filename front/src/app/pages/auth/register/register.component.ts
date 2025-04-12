import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Location } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {
  registerForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private location: Location,
    private router: Router
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

  ngOnInit(): void {}

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
      console.log('Données d\'inscription:', userData);
      
      // Pour l'instant, simplement afficher un message
      // Cette méthode sera mise à jour pour appeler un service d'authentification
      alert('Inscription réussie! (Simulation)');
      this.router.navigate(['/']);
    } else {
      // Marquer tous les champs comme touchés pour afficher les erreurs
      Object.keys(this.registerForm.controls).forEach(key => {
        const control = this.registerForm.get(key);
        control?.markAsTouched();
      });
    }
  }
}
