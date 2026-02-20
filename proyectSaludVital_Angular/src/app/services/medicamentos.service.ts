import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';  // 👈 tu servicio genérico


@Injectable({
  providedIn: 'root'
})
export class MedicamentosService {
  private endpoint = 'medicamentos';

  constructor(private apiService: ApiService) {}

  // Listar (público, no necesita token)
  listar(): Observable<any[]> {
    return this.apiService.get<any[]>(`${this.endpoint}/listado`);
  }

  // Registrar (solo ADMIN, requiere token)
  registrar(medicamento: any): Observable<any> {
    return this.apiService.post<any>(`${this.endpoint}/nuevo`, medicamento);
  }

  // Editar (solo ADMIN, requiere token)
  editar(id: number, medicamento: any): Observable<any> {
    return this.apiService.put<any>(`${this.endpoint}/editar/${id}`, medicamento);
  }
}