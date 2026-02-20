import { Injectable } from "@angular/core";
import { CanActivate, Router } from "@angular/router";

@Injectable({ providedIn: 'root' })



export class AuthService {
  constructor(private router: Router) {}

  guardarToken(token: string) {
    localStorage.setItem('token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  logout() {
  localStorage.removeItem('token');
  sessionStorage.clear();
  this.router.navigate(['/login']);
}

  getUsuario() {
  const token = localStorage.getItem('token');
  if (!token) return null;

  const payload = JSON.parse(atob(token.split('.')[1]));
  console.log("🎟️ Payload del token:", payload);

  return payload;
  
  // aquí debería venir rol o authorities
}

  isTokenExpired(): boolean {
    const token = this.getToken();
    if (!token) return true;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const exp = payload.exp; // en segundos
      const now = Math.floor(Date.now() / 1000);
      return exp < now;
    } catch (error) {
      return true; // si falla el decode, lo tratamos como expirado
    }
}
}