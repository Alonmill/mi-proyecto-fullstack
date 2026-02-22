import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ReporteTablaDTO } from '../DTO/reporte-tabla-dto';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class ReportesService {

  constructor(private apiService: ApiService) {}

  obtenerData(endpoint: string, fecha?: string, agrupacion?: string): Observable<ReporteTablaDTO> {
    const base = `reportes/${endpoint}/data`;
    if (agrupacion) {
      return this.apiService.getWithParams<ReporteTablaDTO>(base, { agrupacion });
    }
    if (fecha) {
      return this.apiService.getWithParams<ReporteTablaDTO>(base, { fecha });
    }
    return this.apiService.get<ReporteTablaDTO>(base);
  }

  exportarPdf(endpoint: string, fecha?: string, agrupacion?: string): Observable<Blob> {
    const base = `reportes/${endpoint}`;
    if (agrupacion) {
      return this.apiService.getBlob(base, { agrupacion });
    }
    if (fecha) {
      return this.apiService.getBlob(base, { fecha });
    }
    return this.apiService.getBlob(base);
  }
}
