import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {
  constructor(private router: Router) {}

  ngOnInit(): void {}

  /**
   * Navigue vers la page spécifiée
   * @param route La route à laquelle naviguer ('login' ou 'register')
   */
  navigateTo(route: string): void {
    // Navigation vers la page spécifiée
    this.router.navigate([route]);
  }
}
