import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TokenStorageService } from '../../services/token-storage.service';
import { ArticleService } from '../../services/article.service';
import { Article } from '../../models/article.model';

@Component({
  selector: 'app-article-detail',
  templateUrl: './article-detail.component.html',
  styleUrls: ['./article-detail.component.scss']
})
export class ArticleDetailComponent implements OnInit {
  article: Article | null = null;
  loading = false;
  error = '';
  currentUser: any = null;
  isMobileMenuOpen = false;
  commentForm: FormGroup;
  isSubmittingComment = false;
  commentSubmitError = '';
  commentSubmitSuccess = '';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private tokenStorage: TokenStorageService,
    private articleService: ArticleService,
    private fb: FormBuilder
  ) {
    this.commentForm = this.fb.group({
      content: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(1000)]]
    });
  }

  ngOnInit(): void {
    this.currentUser = this.tokenStorage.getUser();
    this.loadArticle();
  }

  /**
   * Charge les détails de l'article à partir de son ID dans l'URL
   */
  loadArticle(): void {
    this.loading = true;
    this.error = '';
    
    const articleId = Number(this.route.snapshot.paramMap.get('id'));
    
    if (isNaN(articleId)) {
      this.error = 'ID d\'article invalide';
      this.loading = false;
      return;
    }
    
    this.articleService.getArticleById(articleId).subscribe({
      next: (data) => {
        this.article = data;
        
        // Une fois l'article chargé, récupérer ses commentaires
        this.loadComments(articleId);
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement de l\'article: ' + (err.error?.message || err.message);
        this.loading = false;
        console.error('Erreur:', err);
      }
    });
  }
  
  /**
   * Charge les commentaires d'un article
   * @param articleId Identifiant de l'article
   */
  loadComments(articleId: number): void {
    this.articleService.getCommentsByArticle(articleId).subscribe({
      next: (comments) => {
        if (this.article) {
          this.article.comments = comments;
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des commentaires:', err);
        // On ne bloque pas l'affichage de l'article si les commentaires ne sont pas chargés
        this.loading = false;
      }
    });
  }

  /**
   * Soumet un nouveau commentaire
   */
  submitComment(): void {
    if (this.commentForm.invalid || !this.article) {
      return;
    }
    
    this.isSubmittingComment = true;
    this.commentSubmitError = '';
    this.commentSubmitSuccess = '';
    
    const comment = {
      articleId: this.article.id,
      content: this.commentForm.value.content
    };
    
    this.articleService.addComment(comment).subscribe({
      next: () => {
        this.commentSubmitSuccess = 'Commentaire ajouté avec succès!';
        this.commentForm.reset();
        // Recharger l'article pour afficher le nouveau commentaire
        this.loadArticle();
        this.isSubmittingComment = false;
      },
      error: (err) => {
        this.commentSubmitError = 'Erreur lors de l\'ajout du commentaire: ' + (err.error?.message || err.message);
        this.isSubmittingComment = false;
        console.error('Erreur:', err);
      }
    });
  }

  /**
   * Retourne à la liste des articles
   */
  goBack(): void {
    this.router.navigate(['/articles']);
  }

  // Navigation et fonctions du header (exactement comme les autres pages)
  navigateToArticles(): void {
    this.router.navigate(['/articles']);
  }

  navigateToThemes(): void {
    this.router.navigate(['/themes']);
  }

  navigateToProfile(): void {
    // À implémenter quand la page de profil sera créée
    console.log('Navigation vers le profil');
  }

  logout(): void {
    this.tokenStorage.signOut();
    console.log('Déconnexion réussie');
    this.router.navigate(['/login']);
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }
}
