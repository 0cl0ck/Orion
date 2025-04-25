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

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.articleService.createArticle(this.articleForm.value).subscribe({
      next: (response) => {
        this.loading = false;
        this.successMessage = 'Article créé avec succès!';
        // Rediriger vers la liste des articles après un court délai
        setTimeout(() => {
          this.router.navigate(['/articles']);
        }, 1500);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Une erreur est survenue lors de la création de l\'article';
        console.error('Erreur:', err);
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
