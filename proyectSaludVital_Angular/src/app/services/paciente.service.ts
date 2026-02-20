import { map, Observable } from "rxjs";
import { ObtenerPacienteDTO } from "../DTO/obtener-paciente-dto";
import { PacienteConMostrar } from "../DTO/paciente-enfermedad-alergia";
import { ApiService } from "./api.service";
import { Injectable } from "@angular/core";
import { ObtenerExpedienteMedicoDTO } from "../DTO/paciente-ver-expedi";


@Injectable({
    providedIn: 'root'
})
export class PacienteService{

    private endpoint = 'pacientes'
    constructor(private apiService : ApiService){}

    listar(): Observable<PacienteConMostrar[]> {
    return this.apiService.get<ObtenerPacienteDTO[]>(`${this.endpoint}/listado`)
    .pipe(
      map(data => data.map(m => ({ ...m, showAlergias: false,showEnfermedades: false })))
    );
}
    agregar(paciente: any): Observable<any> {
        return this.apiService.post<any>(`${this.endpoint}/nuevo`, paciente);
      }
    
     actualizar(id: number, paciente: any): Observable<any> {
        return this.apiService.put<any>(`${this.endpoint}/editar/${id}`, paciente);
      }
    // Obtener perfil del paciente (solo con token PACIENTE)
          getPerfil(): Observable<ObtenerPacienteDTO> {
          return this.apiService.get<ObtenerPacienteDTO>(`${this.endpoint}/perfil`);
        }

        // Obtener expediente del paciente (solo con token PACIENTE)
          getExpediente(): Observable<ObtenerExpedienteMedicoDTO> {
          return this.apiService.get<ObtenerExpedienteMedicoDTO>(`${this.endpoint}/expediente`);
        }

}