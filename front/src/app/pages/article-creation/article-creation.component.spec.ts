import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatIconModule } from '@angular/material/icon';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';

import { ArticleCreationComponent } from './article-creation.component';
import { ArticleService } from '../../services/article.service';
import { ThemeService } from '../../services/theme.service';
import { TokenStorageService } from '../../services/token-storage.service';

describe('ArticleCreationComponent', () => {
  let component: ArticleCreationComponent;
  let fixture: ComponentFixture<ArticleCreationComponent>;
  let articleServiceSpy: jasmine.SpyObj<ArticleService>;
  let themeServiceSpy: jasmine.SpyObj<ThemeService>;

  beforeEach(async () => {
    const articleSpy = jasmine.createSpyObj('ArticleService', ['createArticle']);
    const themeSpy = jasmine.createSpyObj('ThemeService', ['getAllThemes']);
    const tokenSpy = jasmine.createSpyObj('TokenStorageService', ['getUser', 'signOut']);

    await TestBed.configureTestingModule({
      declarations: [ ArticleCreationComponent ],
      imports: [
        ReactiveFormsModule,
        RouterTestingModule,
        HttpClientTestingModule,
        MatIconModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: ArticleService, useValue: articleSpy },
        { provide: ThemeService, useValue: themeSpy },
        { provide: TokenStorageService, useValue: tokenSpy }
      ]
    }).compileComponents();

    articleServiceSpy = TestBed.inject(ArticleService) as jasmine.SpyObj<ArticleService>;
    themeServiceSpy = TestBed.inject(ThemeService) as jasmine.SpyObj<ThemeService>;
    
    themeServiceSpy.getAllThemes.and.returnValue(of([
      { id: 1, name: 'Thème 1' },
      { id: 2, name: 'Thème 2' }
    ]));
    
    articleServiceSpy.createArticle.and.returnValue(of({
      id: 1,
      title: 'Article Test',
      content: 'Contenu de test',
      createdAt: new Date().toISOString(),
      authorId: 1,
      authorUsername: 'testuser',
      themeId: 1,
      themeName: 'Thème 1',
      commentCount: 0
    }));
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArticleCreationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with empty values', () => {
    expect(component.articleForm.get('title')?.value).toBe('');
    expect(component.articleForm.get('content')?.value).toBe('');
    expect(component.articleForm.get('themeId')?.value).toBe('');
  });

  it('should mark form as invalid when empty', () => {
    expect(component.articleForm.valid).toBeFalsy();
  });

  it('should mark form as valid when all fields are filled correctly', () => {
    component.articleForm.controls['title'].setValue('Test Article');
    component.articleForm.controls['content'].setValue('This is a test article content with more than 10 chars');
    component.articleForm.controls['themeId'].setValue(1);
    
    expect(component.articleForm.valid).toBeTruthy();
  });

  it('should call createArticle method of ArticleService on form submission', () => {
    component.articleForm.controls['title'].setValue('Test Article');
    component.articleForm.controls['content'].setValue('This is a test article content with more than 10 chars');
    component.articleForm.controls['themeId'].setValue(1);
    
    component.onSubmit();
    
    expect(articleServiceSpy.createArticle).toHaveBeenCalledWith({
      'title': 'Test Article',
      'content': 'This is a test article content with more than 10 chars',
      'themeId': 1
    });
  });
});
