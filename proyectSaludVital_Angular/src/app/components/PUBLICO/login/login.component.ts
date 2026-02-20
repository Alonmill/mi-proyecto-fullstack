import { Component, OnInit } from '@angular/core';

import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RegistroService } from '../../../services/registro.service';
import { AuthService } from '../../../services/auth.service';



@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule], 
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  email = '';
  password = '';

  constructor(
    private registroService: RegistroService,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    // 👇 Limpia cualquier sesión previa al entrar al login
    this.authService.logout();
  }

  onLogin() {
    this.registroService.login({ email: this.email, password: this.password }).subscribe({
      next: (resp) => {
        // Guardar token nuevo
        this.authService.guardarToken(resp.token);

        // Obtener usuario del token
        const usuario = this.authService.getUsuario();

        if (usuario && usuario.rol) {
          switch (usuario.rol) {
            case 'ADMIN':
              this.router.navigate(['/admin']);
              break;
            case 'MEDICO':
              this.router.navigate(['/medico']);
              break;
            case 'PACIENTE':
              this.router.navigate(['/paciente']);
              break;
            default:
              this.router.navigate(['/']);
              break;
          }
        } else {
          alert('No se pudo determinar el rol del usuario');
          this.router.navigate(['/login']);
        }
      },
      error: () => alert('Credenciales inválidas')
    });
  }
}