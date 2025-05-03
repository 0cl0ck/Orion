import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';

// Angular Material
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatMenuModule } from '@angular/material/menu';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBarModule } from '@angular/material/snack-bar';

// Routing
import { AppRoutingModule } from './app-routing.module';

// Components
import { AppComponent } from './app.component';
import { HomeComponent } from './pages/home/home.component';
import { RegisterComponent } from './pages/auth/register/register.component';
import { LoginComponent } from './pages/auth/login/login.component';
import { ArticlesComponent } from './pages/articles/articles.component';
import { ArticleCreationComponent } from './pages/article-creation/article-creation.component';
import { ThemesComponent } from './pages/themes/themes.component';
import { ProfilComponent } from './pages/profil/profil.component';
import { ArticleDetailComponent } from './pages/article-detail/article-detail.component';

// Services
import { authInterceptorProviders } from './services/auth.interceptor';
import { errorInterceptorProviders } from './services/error.interceptor';

@NgModule({
  declarations: [AppComponent, HomeComponent, RegisterComponent, LoginComponent, ArticlesComponent, ArticleCreationComponent, ThemesComponent, ArticleDetailComponent, ProfilComponent],
  imports: [
    // Angular core modules
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    HttpClientModule,
    
    // Angular Material modules
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatMenuModule,
    MatCardModule,
    MatDividerModule,
    MatSnackBarModule,
  ],
  providers: [authInterceptorProviders, errorInterceptorProviders],
  bootstrap: [AppComponent],
})
export class AppModule {}
