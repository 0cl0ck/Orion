import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatIconModule } from '@angular/material/icon';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';

import { ThemesComponent } from './themes.component';
import { ThemeService } from '../../services/theme.service';
import { TokenStorageService } from '../../services/token-storage.service';

describe('ThemesComponent', () => {
  let component: ThemesComponent;
  let fixture: ComponentFixture<ThemesComponent>;
  let themeServiceSpy: jasmine.SpyObj<ThemeService>;
  let tokenStorageSpy: jasmine.SpyObj<TokenStorageService>;

  beforeEach(async () => {
    const themeSpy = jasmine.createSpyObj('ThemeService', ['getAllThemes']);
    const tokenSpy = jasmine.createSpyObj('TokenStorageService', ['getUser', 'signOut']);

    await TestBed.configureTestingModule({
      declarations: [ ThemesComponent ],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
        MatIconModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: ThemeService, useValue: themeSpy },
        { provide: TokenStorageService, useValue: tokenSpy }
      ]
    }).compileComponents();

    themeServiceSpy = TestBed.inject(ThemeService) as jasmine.SpyObj<ThemeService>;
    tokenStorageSpy = TestBed.inject(TokenStorageService) as jasmine.SpyObj<TokenStorageService>;
    
    tokenStorageSpy.getUser.and.returnValue({ id: 1, username: 'testuser' });
    themeServiceSpy.getAllThemes.and.returnValue(of([
      { id: 1, name: 'Thème 1', description: 'Description du thème 1' },
      { id: 2, name: 'Thème 2', description: 'Description du thème 2', isSubscribed: true }
    ]));
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ThemesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load themes on init', () => {
    expect(themeServiceSpy.getAllThemes).toHaveBeenCalled();
    expect(component.themes.length).toBe(2);
  });

  it('should toggle mobile menu', () => {
    expect(component.isMobileMenuOpen).toBeFalse();
    component.toggleMobileMenu();
    expect(component.isMobileMenuOpen).toBeTrue();
    component.toggleMobileMenu();
    expect(component.isMobileMenuOpen).toBeFalse();
  });

  it('should navigate to articles with theme ID when viewing theme articles', () => {
    const navigateSpy = spyOn(component['router'], 'navigate');
    component.viewThemeArticles(1);
    expect(navigateSpy).toHaveBeenCalledWith(['/articles'], { queryParams: { themeId: 1 } });
  });

  it('should handle subscribe to theme', () => {
    const mockEvent = jasmine.createSpyObj('Event', ['stopPropagation']);
    component.subscribeToTheme(1, mockEvent);
    expect(mockEvent.stopPropagation).toHaveBeenCalled();
    expect(component.isSubscribing).toBeTrue();
    expect(component.subscribingThemeId).toBe(1);
  });

  it('should handle unsubscribe from theme', () => {
    const mockEvent = jasmine.createSpyObj('Event', ['stopPropagation']);
    component.unsubscribeFromTheme(2, mockEvent);
    expect(mockEvent.stopPropagation).toHaveBeenCalled();
    expect(component.isSubscribing).toBeTrue();
    expect(component.subscribingThemeId).toBe(2);
  });
});
