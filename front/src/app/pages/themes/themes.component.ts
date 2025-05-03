import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TokenStorageService } from '../../services/token-storage.service';
import { ThemeService } from '../../services/theme.service';
import { Theme } from '../../models/theme.model';

@Component({
  selector: 'app-themes',
  templateUrl: './themes.component.html',
  styleUrls: ['./themes.component.scss']
})
export class ThemesComponent implements OnInit {
  themes: Theme[] = [];
  loading = false;
  error = '';
  currentUser: any = null;
  isMobileMenuOpen = false;
  isSubscribing = false;
  subscribingThemeId: number | null = null;

  constructor(
    private router: Router,
    private tokenStorage: TokenStorageService,
    private themeService: ThemeService
  ) { }

  ngOnInit(): void {
    this.currentUser = this.tokenStorage.getUser();
    this.loadThemes();
  }

  /**
   * Charge tous les thèmes depuis l'API et récupère les abonnements de l'utilisateur
   */
  loadThemes() {
    this.loading = true;
    this.error = '';
    
    // Commencer par récupérer tous les thèmes disponibles
    this.themeService.getAllThemes().subscribe({
      next: (themes) => {
        // Stocker temporairement les thèmes
        const tempThemes = themes;
        
        // Ensuite, récupérer les abonnements de l'utilisateur
        this.themeService.getUserSubscriptions().subscribe({
          next: (subscribedThemeIds) => {
            // Mettre à jour l'état d'abonnement de chaque thème
            this.themes = tempThemes.map(theme => ({
              ...theme,
              isSubscribed: subscribedThemeIds.includes(theme.id)
            }));
            this.loading = false;
          },
          error: (err) => {
            // En cas d'erreur sur les abonnements, utiliser les thèmes sans info d'abonnement
            console.error('Erreur lors du chargement des abonnements:', err);
            this.themes = tempThemes;
            this.loading = false;
          }
        });
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des thèmes: ' + (err.error?.message || err.message);
        this.loading = false;
        console.error('Erreur:', err);
      }
    });
  }

  /**
   * S'abonner à un thème
   * @param themeId Identifiant du thème
   */
  subscribeToTheme(themeId: number, event: Event) {
    event.stopPropagation(); // Empêche la navigation lors du clic sur le bouton
    
    this.isSubscribing = true;
    this.subscribingThemeId = themeId;
    
    this.themeService.subscribeToTheme(themeId).subscribe({
      next: () => {
        console.log(`Abonnement au thème ${themeId}`);
        // Mettre à jour l'état d'abonnement dans la liste des thèmes
        this.themes = this.themes.map(theme => {
          if (theme.id === themeId) {
            return { ...theme, isSubscribed: true };
          }
          return theme;
        });
      },
      error: (err) => {
        console.error('Erreur lors de l\'abonnement:', err);
      },
      complete: () => {
        this.isSubscribing = false;
        this.subscribingThemeId = null;
      }
    });
  }

  /**
   * Se désabonner d'un thème
   * @param themeId Identifiant du thème
   */
  unsubscribeFromTheme(themeId: number, event: Event) {
    event.stopPropagation(); // Empêche la navigation lors du clic sur le bouton
    
    this.isSubscribing = true;
    this.subscribingThemeId = themeId;
    
    this.themeService.unsubscribeFromTheme(themeId).subscribe({
      next: () => {
        console.log(`Désabonnement du thème ${themeId}`);
        // Mettre à jour l'état d'abonnement dans la liste des thèmes
        this.themes = this.themes.map(theme => {
          if (theme.id === themeId) {
            return { ...theme, isSubscribed: false };
          }
          return theme;
        });
      },
      error: (err) => {
        console.error('Erreur lors du désabonnement:', err);
      },
      complete: () => {
        this.isSubscribing = false;
        this.subscribingThemeId = null;
      }
    });
  }

  /**
   * Voir les articles d'un thème
   * @param themeId Identifiant du thème
   */
  viewThemeArticles(themeId: number) {
    this.router.navigate(['/articles'], { queryParams: { themeId: themeId } });
  }

  // Navigation et fonctions du header (exactement comme les autres pages)
  navigateToArticles() {
    this.router.navigate(['/articles']);
  }

  navigateToProfile() {
    // À implémenter quand la page de profil sera créée
    console.log('Navigation vers le profil');
  }

  logout() {
    this.tokenStorage.signOut();
    console.log('Déconnexion réussie');
    this.router.navigate(['/login']);
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }
}
