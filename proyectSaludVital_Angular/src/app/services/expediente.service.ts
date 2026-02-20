import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ExpedienteMedicoDTO } from '../DTO/Expediente-medico-dto';
import { EditarExpedienteDTO } from '../DTO/editar-expediente-dto';
import { EntradaHistorialDTO } from '../DTO/paciente-ver-expedi';
import { ActualizarEntradaDTO } from '../DTO/actualizar-entrada-dto';



@Injectable({
  providedIn: 'root'
})
export class ExpedienteService {
  private endpoint = 'expedientes';

  constructor(private apiService: ApiService) {}

  // GET /listado
  getExpedientes(): Observable<ExpedienteMedicoDTO[]> {
    return this.apiService.get<ExpedienteMedicoDTO[]>(`${this.endpoint}/listado`);
  }

  // GET /ver/{id}
  getExpedienteById(id: number): Observable<ExpedienteMedicoDTO> {
    return this.apiService.get<ExpedienteMedicoDTO>(`${this.endpoint}/ver/${id}`);
  }

  // PUT /editar/{id}
  editarExpediente(id: number, entrada: ActualizarEntradaDTO): Observable<ExpedienteMedicoDTO> {
    return this.apiService.put<ExpedienteMedicoDTO>(`${this.endpoint}/editar/${id}`, entrada);
  }

  // POST /entrada/nueva/{idPaciente}
  agregarEntradaHistorial(idPaciente: number, dto: { citaId: number; diagnostico: string; tratamiento: string }): Observable<EntradaHistorialDTO> {
    return this.apiService.post<EntradaHistorialDTO>(`${this.endpoint}/entrada/nueva/${idPaciente}`, dto);
  }
}
