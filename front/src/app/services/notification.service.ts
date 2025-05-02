import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject } from 'rxjs';

export enum NotificationType {
  SUCCESS = 'success',
  ERROR = 'error',
  INFO = 'info',
  WARNING = 'warning'
}

export interface Notification {
  message: string;
  type: NotificationType;
  duration?: number;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notificationSubject = new Subject<Notification>();
  notification$ = this.notificationSubject.asObservable();

  constructor(private snackBar: MatSnackBar) {
    // S'abonner aux notifications pour les afficher automatiquement
    this.notification$.subscribe(notification => {
      this.showSnackBar(notification);
    });
  }

  /**
   * Affiche une notification de succès
   * @param message Message à afficher
   * @param duration Durée d'affichage en millisecondes (défaut: 3000ms)
   */
  success(message: string, duration = 3000): void {
    this.notify(message, NotificationType.SUCCESS, duration);
  }

  /**
   * Affiche une notification d'erreur
   * @param message Message à afficher
   * @param duration Durée d'affichage en millisecondes (défaut: 5000ms)
   */
  error(message: string, duration = 5000): void {
    this.notify(message, NotificationType.ERROR, duration);
  }

  /**
   * Affiche une notification d'information
   * @param message Message à afficher
   * @param duration Durée d'affichage en millisecondes (défaut: 3000ms)
   */
  info(message: string, duration = 3000): void {
    this.notify(message, NotificationType.INFO, duration);
  }

  /**
   * Affiche une notification d'avertissement
   * @param message Message à afficher
   * @param duration Durée d'affichage en millisecondes (défaut: 4000ms)
   */
  warning(message: string, duration = 4000): void {
    this.notify(message, NotificationType.WARNING, duration);
  }

  /**
   * Méthode générique pour créer une notification
   */
  private notify(message: string, type: NotificationType, duration: number): void {
    this.notificationSubject.next({ message, type, duration });
  }

  /**
   * Affiche un snackbar avec la notification
   */
  private showSnackBar(notification: Notification): void {
    const { message, type, duration = 3000 } = notification;
    
    this.snackBar.open(message, 'Fermer', {
      duration: duration,
      horizontalPosition: 'center',
      verticalPosition: 'bottom',
      panelClass: [`notification-${type}`]
    });
  }
}
