import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css'
})
export class MenuComponent {

  menuItems = [
    { label: 'Iniciar sesion', route: '/login' },
    { label: 'Registro', route: '/registro' },
    { label: 'Acerca de', route: '/acerca'},
    { label: 'Cerrar Sesion', action: 'logout' }
  ];

  constructor(private authService: AuthService, private router: Router) {}

   get isLoggedIn(): boolean {
    return this.authService.isLoggedIn() && !this.authService.isTokenExpired();
  }

  onMenuClick(item: any) {
    if (item.action === 'logout') {
      this.authService.logout();
    }
  }

  goToInicio(): void {
    this.router.navigate(['/login']);
  }
}
