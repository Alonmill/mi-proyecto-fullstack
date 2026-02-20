import { Injectable } from "@angular/core";
import { ApiService } from "./api.service";
import { map, Observable } from "rxjs";
import { ObtenerMedicoDTO } from "../DTO/obtener-medico-dto";
import { MedicoConMostrar } from "../DTO/medico-horario";

@Injectable({
    providedIn: 'root'
})
export class MedicoService{

    private endpoint = 'medicos'
    constructor(private apiService : ApiService){}

    listar(): Observable<MedicoConMostrar[]> {
  return this.apiService.get<ObtenerMedicoDTO[]>(`${this.endpoint}/listado`)
    .pipe(
      map(data => data.map(m => ({ ...m, showHorarios: false })))
    );
}

   agregar(medico: any): Observable<any> {
    return this.apiService.post<any>(`${this.endpoint}/nuevo`, medico);
  }

 actualizar(id: number, medico: any): Observable<any> {
    return this.apiService.put<any>(`${this.endpoint}/editar/${id}`, medico);
  }
// Obtener perfil del medico (solo con token MEDICO)
      getPerfil(): Observable<ObtenerMedicoDTO> {
      return this.apiService.get<ObtenerMedicoDTO>(`${this.endpoint}/perfil`);
    }

}