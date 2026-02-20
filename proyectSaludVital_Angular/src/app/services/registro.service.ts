import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class RegistroService {
  private endpoint = 'register';

  constructor(private apiService: ApiService) { }

  // Método para registrar un nuevo usuario
  registrarUsuario(usuario: any): Observable<string> { // 👈 devuelve texto plano
    return this.apiService.post<string>('register', usuario, true);
  }

  // Método para validar credenciales y obtener token
  login(credenciales: { email: string, password: string }): Observable<any> {
    return this.apiService.post<any>('login', credenciales);
  }

  // Método para guardar el token (ya no usa apiService)
  guardarToken(token: string): void {
    localStorage.setItem('token', token); // 👈 ahora se guarda aquí
  }
}