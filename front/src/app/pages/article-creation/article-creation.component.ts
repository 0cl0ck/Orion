import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TokenStorageService } from '../../services/token-storage.service';
import { ArticleService } from '../../services/article.service';
import { ThemeService } from '../../services/theme.service';
import { Theme } from '../../models/theme.model';

@Component({
  selector: 'app-article-creation',
  templateUrl: './article-creation.component.html',
  styleUrls: ['./article-creation.component.scss']
})
export class ArticleCreationComponent implements OnInit {
  articleForm: FormGroup;
  themes: Theme[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';
  isMobileMenuOpen = false;
  currentUser: any = null;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private tokenStorage: TokenStorageService,
    private articleService: ArticleService,
    private themeService: ThemeService
  ) {
    this.articleForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      content: ['', [Validators.required, Validators.minLength(10)]],
      themeId: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.currentUser = this.tokenStorage.getUser();
    this.loadThemes();
  }

  loadThemes(): void {
    this.themeService.getAllThemes().subscribe({
      next: (data) => {
        this.themes = data;
        
        if (this.themes && this.themes.length > 0) {
          // Utiliser simplement le premier thème disponible dans la liste
          const defaultTheme = this.themes[0];
          this.articleForm.patchValue({
            themeId: defaultTheme.id
          });
        }
      },
      error: (err) => {
        console.error('Erreur lors du chargement des thèmes:', err);
        this.errorMessage = 'Impossible de charger les thèmes. Veuillez réessayer plus tard.';
      }
    });
  }

  onSubmit(): void {
    if (this.articleForm.invalid) {
      return;
    }

    console.log('=== SOUMISSION DU FORMULAIRE ===');
    console.log('Valeurs du formulaire:', this.articleForm.value);
    console.log('État du formulaire:', {
      valid: this.articleForm.valid,
      dirty: this.articleForm.dirty,
      touched: this.articleForm.touched
    });
    
    // Vérifier si l'utilisateur est connecté
    const userInfo = this.tokenStorage.getUser();
    const token = this.tokenStorage.getToken();
    console.log('Utilisateur connecté:', !!userInfo);
    if (userInfo) {
      console.log('Informations utilisateur:', { 
        id: userInfo.id,
        email: userInfo.email,
        username: userInfo.username,
        roles: userInfo.roles
      });
    }
    console.log('Token présent:', !!token);

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';
    
    // Récupérer toutes les valeurs du formulaire
    const formValues = this.articleForm.value;
    
    // Vérifier que le themeId est valide (qu'il existe dans la liste des thèmes récupérés)
    // Sinon, utiliser le premier thème disponible comme fallback
    const themeId = parseInt(formValues.themeId, 10);
    const validatedThemeId = this.themes.some(t => t.id === themeId) ? themeId : (this.themes.length > 0 ? this.themes[0].id : null);
    
    console.log('Validation du themeId:', {
      original: formValues.themeId,
      parsed: themeId,
      validated: validatedThemeId,
      themesAvailable: this.themes.map(t => ({ id: t.id, name: t.name }))
    });
    
    // Vérification finale qu'un thème est bien sélectionné
    if (validatedThemeId === null) {
      this.errorMessage = 'Aucun thème disponible. Impossible de créer l\'article.';
      this.loading = false;
      return;
    }
    
    // Création de l'article avec un themeId validé
    const articleData = {
      title: formValues.title,
      content: formValues.content,
      themeId: validatedThemeId
    };

    // Appel au service avec les données validées
    this.articleService.createArticle(articleData).subscribe({
      next: (response) => {
        console.log('=== ARTICLE CRÉÉ AVEC SUCCÈS ===');
        console.log('Réponse reçue:', response);
        this.loading = false;
        this.successMessage = 'Article créé avec succès!';
        // Rediriger vers la liste des articles après un court délai
        setTimeout(() => {
          this.router.navigate(['/articles']);
        }, 1500);
      },
      error: (err) => {
        console.error('=== ERREUR LORS DE LA CRÉATION D\'ARTICLE ===');
        console.error('Status:', err.status);
        console.error('Status Text:', err.statusText);
        console.error('Message d\'erreur:', err.error?.message || 'Aucun message d\'erreur');
        console.error('Erreur complète:', err);
        
        this.loading = false;
        
        if (err.status === 401) {
          this.errorMessage = 'Erreur d\'authentification. Veuillez vous reconnecter.';
          // Vérifier l'état actuel du token
          const currentToken = this.tokenStorage.getToken();
          console.error('État actuel du token (après erreur):', !!currentToken);
          
          // Tenter de récupérer les entêtes de la réponse
          if (err.headers) {
            console.error('En-têtes de la réponse d\'erreur:');
            err.headers.keys().forEach((key: string) => {
              console.error(`  ${key}: ${err.headers.get(key)}`);
            });
          }
        } else {
          this.errorMessage = err.error?.message || 'Une erreur est survenue lors de la création de l\'article';
        }
      }
    });
  }

  // Getter pour faciliter l'accès au formulaire dans le template
  get f() {
    return this.articleForm.controls;
  }

  // Navigation et fonctions du header
  navigateToArticles(): void {
    this.router.navigate(['/articles']);
  }

  navigateToThemes(): void {
    // À implémenter quand la page des thèmes sera créée
    console.log('Navigation vers les thèmes');
  }

  navigateToProfile(): void {
    // À implémenter quand la page de profil sera créée
    console.log('Navigation vers le profil');
  }

  logout(): void {
    this.tokenStorage.signOut();
    this.router.navigate(['/login']);
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }
}
