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
        this.articles = data;
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
      this.articles.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
    }
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
