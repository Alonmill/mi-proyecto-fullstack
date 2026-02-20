import { ObtenerPacienteDTO } from "./obtener-paciente-dto";


export interface PacienteConMostrar extends ObtenerPacienteDTO {
  showAlergias: boolean;
  showEnfermedades: boolean;
}