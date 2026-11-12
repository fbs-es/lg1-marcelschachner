import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('zeigt die Navigation zu Katalog und Kunden', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const link = (fixture.nativeElement as HTMLElement).querySelector('nav a');
    expect(link?.textContent).toContain('Katalog');
    expect(link?.getAttribute('href')).toBe('/produkte');
    const kunden = (fixture.nativeElement as HTMLElement).querySelectorAll('nav a')[1];
    expect(kunden.textContent).toContain('Kunden');
    expect(kunden.getAttribute('href')).toBe('/kunden');
  });
});
