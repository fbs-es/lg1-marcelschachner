import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { Warenkorb } from './bestellung/warenkorb';

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

  it('zeigt die Stückzahl im Warenkorb in der Navigation', async () => {
    const fixture = TestBed.createComponent(App);
    TestBed.inject(Warenkorb).lege(
      {
        id: 'p1',
        titel: 'Buch',
        listenpreis: { cent: 1250, waehrung: 'EUR' },
        lagerbestand: 10,
        istVerfuegbar: true,
      },
      3,
    );
    await fixture.whenStable();
    const link = (fixture.nativeElement as HTMLElement).querySelector('nav a[href="/checkout"]')!;
    expect(link.textContent).toContain('Warenkorb (3)');
    expect(link.getAttribute('href')).toBe('/checkout');
  });
});
