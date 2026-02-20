import { ObtenerMedicoDTO } from "./obtener-medico-dto";


export interface MedicoConMostrar extends ObtenerMedicoDTO {
  showHorarios: boolean;
}