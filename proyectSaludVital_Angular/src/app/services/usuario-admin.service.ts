import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { UsuarioBusquedaDTO } from '../DTO/usuario-busqueda-dto';

@Injectable({
  providedIn: 'root'
})
export class UsuarioAdminService {
  constructor(private apiService: ApiService) {}

  buscarPorEmail(email: string): Observable<UsuarioBusquedaDTO> {
    return this.apiService.get<UsuarioBusquedaDTO>(`admin/usuarios/buscar?email=${encodeURIComponent(email)}`);
  }

  autocompletePorEmail(query: string): Observable<UsuarioBusquedaDTO[]> {
    return this.apiService.get<UsuarioBusquedaDTO[]>(`admin/usuarios/autocomplete?query=${encodeURIComponent(query)}`);
  }
}
