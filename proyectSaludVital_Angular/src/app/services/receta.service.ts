import { Injectable } from "@angular/core";
import { ApiService } from "./api.service";
import { ObtenerRecetaDTO } from "../DTO/obtener-receta-dto";
import { RecetaConMostrar } from "../DTO/receta-items";
import { map, Observable } from "rxjs";


@Injectable({
    providedIn: 'root'
})
export class RecetaService{
private endpoint = 'recetas'
    constructor(private apiService : ApiService){}

    listar(): Observable<RecetaConMostrar[]> {
  return this.apiService.get<ObtenerRecetaDTO[]>(`${this.endpoint}/listado`)
    .pipe(
      map(data => data.map(m => ({ ...m, showItems: false })))
    );
}
    agregar(idPaciente: number, receta: any): Observable<any> {
    return this.apiService.post<any>(`${this.endpoint}/nueva/${idPaciente}`, receta);
  }

actualizar(receta: any): Observable<any> {
  return this.apiService.put<any>(`${this.endpoint}/actualizar`, receta);
}

  // Obtener recetas del paciente (solo con token PACIENTE)
      getRecetas(): Observable<ObtenerRecetaDTO[]> {
  return this.apiService.get<ObtenerRecetaDTO[]>(`${this.endpoint}/mis-recetas`);
}

}