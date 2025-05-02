import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatIconModule } from '@angular/material/icon';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';

import { ArticleDetailComponent } from './article-detail.component';
import { ArticleService } from '../../services/article.service';
import { TokenStorageService } from '../../services/token-storage.service';

describe('ArticleDetailComponent', () => {
  let component: ArticleDetailComponent;
  let fixture: ComponentFixture<ArticleDetailComponent>;
  let articleServiceSpy: jasmine.SpyObj<ArticleService>;
  let tokenStorageSpy: jasmine.SpyObj<TokenStorageService>;

  beforeEach(async () => {
    const articleSpy = jasmine.createSpyObj('ArticleService', ['getArticleById', 'addComment']);
    const tokenSpy = jasmine.createSpyObj('TokenStorageService', ['getUser', 'signOut']);

    await TestBed.configureTestingModule({
      declarations: [ ArticleDetailComponent ],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
        MatIconModule,
        ReactiveFormsModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: ArticleService, useValue: articleSpy },
        { provide: TokenStorageService, useValue: tokenSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ id: '1' })
            }
          }
        }
      ]
    }).compileComponents();

    articleServiceSpy = TestBed.inject(ArticleService) as jasmine.SpyObj<ArticleService>;
    tokenStorageSpy = TestBed.inject(TokenStorageService) as jasmine.SpyObj<TokenStorageService>;
    
    tokenStorageSpy.getUser.and.returnValue({ id: 1, username: 'testuser' });
    articleServiceSpy.getArticleById.and.returnValue(of({
      id: 1,
      title: 'Test Article',
      content: 'This is a test article content',
      authorId: 2,
      authorUsername: 'author',
      themeId: 3,
      themeName: 'Theme',
      createdAt: new Date().toISOString(),
      commentCount: 1,
      comments: [
        { id: 1, content: 'Test comment', authorUsername: 'commenter' }
      ]
    }));
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArticleDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load article on init', () => {
    expect(articleServiceSpy.getArticleById).toHaveBeenCalledWith(1);
    expect(component.article).toBeTruthy();
    expect(component.article?.title).toBe('Test Article');
  });

  it('should handle article load error', () => {
    articleServiceSpy.getArticleById.and.returnValue(throwError(() => new Error('Test error')));
    component.loadArticle();
    expect(component.error).toContain('Erreur lors du chargement');
  });

  it('should submit comment successfully', () => {
    articleServiceSpy.addComment.and.returnValue(of({ success: true }));
    component.commentForm.setValue({ content: 'New comment' });
    component.submitComment();
    expect(articleServiceSpy.addComment).toHaveBeenCalled();
    expect(component.commentSubmitSuccess).toContain('succès');
  });

  it('should handle comment submit error', () => {
    articleServiceSpy.addComment.and.returnValue(throwError(() => new Error('Comment error')));
    component.commentForm.setValue({ content: 'New comment' });
    component.submitComment();
    expect(component.commentSubmitError).toContain('Erreur');
  });

  it('should navigate back to articles', () => {
    const navigateSpy = spyOn(component['router'], 'navigate');
    component.goBack();
    expect(navigateSpy).toHaveBeenCalledWith(['/articles']);
  });

  it('should toggle mobile menu', () => {
    expect(component.isMobileMenuOpen).toBeFalse();
    component.toggleMobileMenu();
    expect(component.isMobileMenuOpen).toBeTrue();
    component.toggleMobileMenu();
    expect(component.isMobileMenuOpen).toBeFalse();
  });
});
