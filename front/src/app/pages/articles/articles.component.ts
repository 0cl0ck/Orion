import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TokenStorageService } from '../../services/token-storage.service';
import { ArticleService } from '../../services/article.service';
import { Article } from '../../models/article.model';

// L'interface a été déplacée dans son propre fichier modèle

@Component({
  selector: 'app-articles',
  templateUrl: './articles.component.html',
  styleUrls: ['./articles.component.scss']
})
export class ArticlesComponent implements OnInit {
  articles: Article[] = [];
  loading = false;
  error = '';
  userSubscriptions: number[] = [];

  sortOrder: 'newest' | 'oldest' = 'newest'; // Default sort order
  currentUser: any = null;
  isMobileMenuOpen = false; // Pour contrôler l'état du menu mobile

  constructor(
    private router: Router,
    private tokenStorage: TokenStorageService,
    private articleService: ArticleService
  ) { }

  ngOnInit(): void {
    this.currentUser = this.tokenStorage.getUser();
    this.loadArticles();
  }

  /**
   * Charge tous les articles depuis l'API
   */
  loadArticles() {
    this.loading = true;
    this.error = '';
    
    this.articleService.getAllArticles().subscribe({
      next: (data) => {
        // Prétraiter les dates avant de les assigner
        this.articles = data.map(article => {
          return {
            ...article,
            // Convertir le format de date problématique en format ISO
            createdAt: this.fixDateFormat(article.createdAt)
          };
        });
        
        this.sortArticles();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des articles: ' + (err.error?.message || err.message);
        this.loading = false;
        console.error('Erreur:', err);
      }
    });
  }
  
  /**
   * Trie les articles par date
   */
  sortArticles() {
    if (this.sortOrder === 'newest') {
      this.articles.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    } else {
      this.articles.sort((a, b) => new Date(a.createdAt).getTime() - new Date(a.createdAt).getTime());
    }
  }
  
  /**
   * Corrige le format de date problématique
   * @param dateStr La chaîne de date à corriger
   * @returns Une chaîne de date au format ISO ou la chaîne originale si non reconnue
   */
  private fixDateFormat(dateStr: string): string {
    // Vérifier si le format correspond au format problématique (nombres séparés par des virgules)
    if (dateStr && dateStr.includes(',')) {
      try {
        // Diviser la chaîne en parties numériques
        const parts = dateStr.split(',').map(part => parseInt(part.trim(), 10));
        
        // Si nous avons au moins 3 parties (année, mois, jour)
        if (parts.length >= 3) {
          // Créer une date avec les composantes
          // Note: les mois en JavaScript sont indexés à partir de 0, donc il faut soustraire 1
          const date = new Date(parts[0], parts[1] - 1, parts[2]);
          
          // Ajouter heures, minutes, secondes si disponibles
          if (parts.length >= 6) {
            date.setHours(parts[3], parts[4], parts[5]);
          }
          
          // Retourner la date au format ISO pour que le pipe date puisse la traiter
          return date.toISOString();
        }
      } catch (e) {
        console.error('Erreur lors de la conversion de la date:', e);
      }
    }
    
    // Retourner la chaîne originale si nous ne pouvons pas la corriger
    return dateStr;
  }

  changeSortOrder(order: 'newest' | 'oldest') {
    this.sortOrder = order;
    this.sortArticles();
  }

  navigateToArticleDetail(articleId: number) {
    this.router.navigate(['/articles', articleId]);
  }

  createNewArticle() {
    this.router.navigate(['/articles/new']);
  }

  navigateToThemes() {
    this.router.navigate(['/themes']);
  }

  navigateToProfile() {
    // To be implemented when profile page is created
    console.log('Navigating to profile');
  }

  logout() {
    this.tokenStorage.signOut();
    console.log('Déconnexion réussie');
    this.router.navigate(['/login']);
  }

  toggleSortDirection() {
    // Toggle between newest and oldest
    this.changeSortOrder(this.sortOrder === 'newest' ? 'oldest' : 'newest');
  }

  /**
   * Bascule l'état du menu mobile
   */
  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }
}
