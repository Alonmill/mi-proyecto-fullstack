import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ActualizarRecetaDTO } from '../DTO/actualizar-receta-dto';
import { ObtenerRecetaDTO } from '../DTO/obtener-receta-dto';
import { RecetaConMostrar } from '../DTO/receta-items';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class RecetaService {
  private endpoint = 'recetas';

  constructor(private apiService: ApiService) {}

  listar(): Observable<RecetaConMostrar[]> {
    return this.apiService
      .get<ObtenerRecetaDTO[]>(`${this.endpoint}/listado`)
      .pipe(map(data => data.map(m => ({ ...m, showItems: false }))));
  }

  agregar(idPaciente: number, receta: any): Observable<any> {
    return this.apiService.post<any>(`${this.endpoint}/nueva/${idPaciente}`, receta);
  }

  actualizar(receta: ActualizarRecetaDTO): Observable<ObtenerRecetaDTO> {
    return this.apiService.put<ObtenerRecetaDTO>(`${this.endpoint}/actualizar`, receta);
  }

  anular(idReceta: number): Observable<ObtenerRecetaDTO> {
    return this.apiService.put<ObtenerRecetaDTO>(`${this.endpoint}/${idReceta}/anular`, {});
  }

  duplicar(idReceta: number): Observable<ObtenerRecetaDTO> {
    return this.apiService.post<ObtenerRecetaDTO>(`${this.endpoint}/${idReceta}/duplicar`, {});
  }

  renovar(idReceta: number): Observable<ObtenerRecetaDTO> {
    return this.apiService.post<ObtenerRecetaDTO>(`${this.endpoint}/${idReceta}/renovar`, {});
  }

  getRecetas(): Observable<ObtenerRecetaDTO[]> {
    return this.apiService.get<ObtenerRecetaDTO[]>(`${this.endpoint}/mis-recetas`);
  }
}
