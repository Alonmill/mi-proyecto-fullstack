import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { CitaObtenidaDTO } from '../DTO/cita-obtenida-dto';
import { CrearCitaDTO } from '../DTO/crear-cita-dto';
import { ActualizarCitaDTO } from '../DTO/actualizar-cita-dto';


@Injectable({
  providedIn: 'root'
})
export class CitaService {
  private endpoint = 'citas';

  constructor(private api: ApiService) {}

  listar(): Observable<CitaObtenidaDTO[]> {
    return this.api.get<CitaObtenidaDTO[]>(`${this.endpoint}/listado`);
  }

  listarProgramadas(): Observable<CitaObtenidaDTO[]> {
    return this.api.get<CitaObtenidaDTO[]>(`${this.endpoint}/programadas`);
  }

  misCitas(): Observable<CitaObtenidaDTO[]> {
    return this.api.get<CitaObtenidaDTO[]>(`${this.endpoint}/mis-citas`);
  }

  crear(cita: CrearCitaDTO): Observable<CitaObtenidaDTO> {
    return this.api.post<CitaObtenidaDTO>(`${this.endpoint}/nueva`, cita);
  }

  actualizar(id: number, cita: ActualizarCitaDTO): Observable<CitaObtenidaDTO> {
    return this.api.put<CitaObtenidaDTO>(`${this.endpoint}/editar/${id}`, cita);
  }

  cancelar(id: number): Observable<any> {
    return this.api.put<any>(`${this.endpoint}/cancelar/${id}`, null);
  }
}