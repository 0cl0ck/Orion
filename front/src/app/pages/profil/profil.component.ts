import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TokenStorageService } from '../../services/token-storage.service';
import { ThemeService } from '../../services/theme.service';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { Theme } from '../../models/theme.model';

@Component({
  selector: 'app-profil',
  templateUrl: './profil.component.html',
  styleUrls: ['./profil.component.scss']
})
export class ProfilComponent implements OnInit {
  currentUser: any = null;
  subscribedThemes: Theme[] = [];
  loading = false;
  error = '';
  isMobileMenuOpen = false;
  
  // Propriétés pour le formulaire de profil
  profileForm: FormGroup;
  isUpdating = false;
  updateSuccess = false;
  
  // Propriétés pour la gestion des abonnements
  isUnsubscribing = false;
  unsubscribingThemeId: number | null = null;

  constructor(
    private router: Router,
    private tokenStorage: TokenStorageService,
    private themeService: ThemeService,
    private userService: UserService,
    private authService: AuthService,
    private formBuilder: FormBuilder
  ) {
    // Initialisation du formulaire
    this.profileForm = this.formBuilder.group({
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.minLength(6)]
    });
  }

  ngOnInit(): void {
    this.currentUser = this.tokenStorage.getUser();
    if (!this.currentUser) {
      // Rediriger vers la page de connexion si non connecté
      this.router.navigate(['/login']);
      return;
    }
    
    // Initialiser les valeurs du formulaire avec les données de l'utilisateur
    this.profileForm.patchValue({
      username: this.currentUser.username,
      email: this.currentUser.email,
      password: '' // Le mot de passe est vide par défaut
    });
    
    this.loadUserSubscriptions();
  }

  /**
   * Charge les thèmes auxquels l'utilisateur est abonné
   */
  loadUserSubscriptions() {
    this.loading = true;
    this.error = '';
    
    // Récupérer d'abord les IDs des thèmes auxquels l'utilisateur est abonné
    this.themeService.getUserSubscriptions().subscribe({
      next: (subscribedThemeIds) => {
        if (subscribedThemeIds.length === 0) {
          this.subscribedThemes = [];
          this.loading = false;
          return;
        }
        
        // Ensuite, récupérer tous les thèmes disponibles
        this.themeService.getAllThemes().subscribe({
          next: (allThemes) => {
            // Filtrer pour ne garder que les thèmes auxquels l'utilisateur est abonné
            this.subscribedThemes = allThemes.filter(theme => 
              subscribedThemeIds.includes(theme.id)
            ).map(theme => ({
              ...theme,
              isSubscribed: true // Ils sont forcément abonnés dans ce contexte
            }));
            this.loading = false;
          },
          error: (err) => {
            this.error = 'Erreur lors du chargement des thèmes: ' + (err.error?.message || err.message);
            this.loading = false;
            console.error('Erreur:', err);
          }
        });
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des abonnements: ' + (err.error?.message || err.message);
        this.loading = false;
        console.error('Erreur:', err);
      }
    });
  }

  /**
   * Se désabonner d'un thème
   * @param themeId Identifiant du thème
   */
  unsubscribeFromTheme(themeId: number, event: Event) {
    event.stopPropagation(); // Empêche la navigation lors du clic sur le bouton
    
    this.isUnsubscribing = true;
    this.unsubscribingThemeId = themeId;
    
    this.themeService.unsubscribeFromTheme(themeId).subscribe({
      next: () => {
        // Retirer le thème de la liste des abonnements
        this.subscribedThemes = this.subscribedThemes.filter(theme => theme.id !== themeId);
      },
      error: (err) => {
        this.error = 'Erreur lors du désabonnement';
      },
      complete: () => {
        this.isUnsubscribing = false;
        this.unsubscribingThemeId = null;
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

  // Navigation et fonctions du header
  navigateToArticles() {
    this.router.navigate(['/articles']);
  }

  navigateToThemes() {
    this.router.navigate(['/themes']);
  }

  logout() {
    this.tokenStorage.signOut();
    // Déconnexion effectuée
    this.router.navigate(['/login']);
  }

  /**
   * Met à jour le profil de l'utilisateur
   */
  updateProfile(): void {
    if (this.profileForm.invalid) {
      return;
    }
    
    this.isUpdating = true;
    this.error = '';
    
    const formValues = this.profileForm.value;
    
    // Créer l'objet de mise à jour avec tous les champs obligatoires
    // Le backend exige que l'email soit toujours présent, même s'il n'a pas changé
    const updateData: any = {
      // Toujours inclure l'email actuel pour satisfaire la validation backend
      email: formValues.email,
      // Toujours inclure le nom d'utilisateur actuel
      username: formValues.username
    };
    
    // Vérifier si des champs ont été modifiés
    const usernameChanged = formValues.username !== this.currentUser.username;
    const emailChanged = formValues.email !== this.currentUser.email;
    
    // Gérer le mot de passe séparément - seulement si rempli
    if (formValues.password && formValues.password.trim() !== '') {
      updateData.password = formValues.password;
    }
    
    // Ne rien faire si aucun champ n'a été modifié
    if (!usernameChanged && !emailChanged && !updateData.password) {
      this.isUpdating = false;
      return;
    }
    
    console.log('Données à mettre à jour:', updateData);
    
    // Appeler le service backend pour mettre à jour le profil
    this.userService.updateUser(this.currentUser.id, updateData).subscribe({
      next: (updatedUser) => {
        console.log('Réponse du serveur après mise à jour:', updatedUser);
        
        // Mettre à jour les informations locales de l'utilisateur
        this.currentUser = {
          ...this.currentUser,
          username: updatedUser.username,
          email: updatedUser.email
        };
        
        // Sauvegarder l'utilisateur mis à jour dans le stockage local
        this.tokenStorage.saveUser(this.currentUser);
        
        // Vérifier si le nom d'utilisateur ou l'email a été modifié
        if (updateData.username || updateData.email) {
          // Si oui, récupérer un nouveau token
          this.getNewToken();
        }
        
        // Réinitialiser le champ de mot de passe
        this.profileForm.patchValue({ password: '' });
        
        // Afficher un message de succès
        this.error = ''; // Effacer les erreurs précédentes
        this.isUpdating = false;
        
        // Ajouter un message de succès temporaire
        this.updateSuccess = true;
        setTimeout(() => {
          this.updateSuccess = false;
        }, 3000);
      },
      error: (err) => {
        // Gérer différentes formes d'objets d'erreur
        if (typeof err === 'object') {
          // Si l'erreur contient directement un message (format intercepteur)
          if (err.message) {
            this.error = err.message;
          }
          // Si l'erreur est un HttpErrorResponse original
          else if (err.error?.message) {
            this.error = err.error.message;
          }
          // Si l'erreur a un 'originalError' (structure observée dans les logs)
          else if (err.originalError?.message) {
            this.error = err.originalError.message;
          }
          // Fallback générique
          else {
            this.error = 'Erreur lors de la mise à jour du profil';
          }
        } else {
          // Si l'erreur est une chaîne de caractères simple
          this.error = err.toString();
        }
        
        this.isUpdating = false;
        console.error('Erreur:', err);
        
        // S'assurer que l'erreur est visible à l'utilisateur
        // Faire défiler la page vers l'élément d'erreur
        setTimeout(() => {
          const errorElement = document.querySelector('.profile-error');
          if (errorElement) {
            errorElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
          }
        }, 100);
        
        // Recharger les abonnements en cas d'erreur pour maintenir l'affichage
        this.loadUserSubscriptions();
      }
    });
  }
  
  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }
  
  /**
   * Obtient un nouveau token JWT du backend
   * Cette méthode est appelée après une mise à jour du profil pour s'assurer 
   * que le token reste valide avec les nouvelles informations d'identité
   */
  private getNewToken(): void {
    console.log('ProfilComponent: Récupération d\'un nouveau token après mise à jour du profil');
    
    // Récupérer le profil utilisateur mis à jour avec le token actuel
    this.userService.getCurrentUser().subscribe({
      next: (userData) => {
        console.log('Profil utilisateur récupéré avec succès après mise à jour:', userData);
        
        // Mettre à jour les informations utilisateur dans le stockage local
        this.tokenStorage.saveUser(userData);
        this.currentUser = userData;
        
        console.log('Profil utilisateur sauvegardé avec succès dans le stockage local');
      },
      error: (err) => {
        // En cas d'erreur, nous avons probablement besoin de reconnecter l'utilisateur
        console.error('Erreur lors de la récupération du profil utilisateur:', err);
      }
    });
  }
}
