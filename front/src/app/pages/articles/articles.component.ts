import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TokenStorageService } from '../../services/token-storage.service';

interface Article {
  id: number;
  title: string;
  content: string;
  date: Date;
  author: string;
}

@Component({
  selector: 'app-articles',
  templateUrl: './articles.component.html',
  styleUrls: ['./articles.component.scss']
})
export class ArticlesComponent implements OnInit {
  // Mock articles data
  articles: Article[] = [
    {
      id: 1,
      title: 'Titre de l\'article',
      content: 'Content: lorem ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry\'s standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled...',
      date: new Date(),
      author: 'Auteur'
    },
    {
      id: 2,
      title: 'Titre de l\'article',
      content: 'Content: lorem ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry\'s standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled...',
      date: new Date(Date.now() - 86400000), // Yesterday
      author: 'Auteur'
    },
    {
      id: 3,
      title: 'Titre de l\'article',
      content: 'Content: lorem ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry\'s standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled...',
      date: new Date(Date.now() - 2 * 86400000), // 2 days ago
      author: 'Auteur'
    },
    {
      id: 4,
      title: 'Titre de l\'article',
      content: 'Content: lorem ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry\'s standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled...',
      date: new Date(Date.now() - 3 * 86400000), // 3 days ago
      author: 'Auteur'
    }
  ];

  sortOrder: 'newest' | 'oldest' = 'newest'; // Default sort order
  currentUser: any = null;

  constructor(
    private router: Router,
    private tokenStorage: TokenStorageService
  ) { }

  ngOnInit(): void {
    this.currentUser = this.tokenStorage.getUser();
    this.sortArticles();
  }

  sortArticles() {
    if (this.sortOrder === 'newest') {
      this.articles.sort((a, b) => b.date.getTime() - a.date.getTime());
    } else {
      this.articles.sort((a, b) => a.date.getTime() - b.date.getTime());
    }
  }

  changeSortOrder(order: 'newest' | 'oldest') {
    this.sortOrder = order;
    this.sortArticles();
  }

  navigateToArticleDetail(articleId: number) {
    // To be implemented when article detail page is created
    console.log(`Navigating to article ${articleId}`);
  }

  createNewArticle() {
    // To be implemented when article creation page is created
    console.log('Creating new article');
  }

  navigateToThemes() {
    // To be implemented when themes page is created
    console.log('Navigating to themes');
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
}
