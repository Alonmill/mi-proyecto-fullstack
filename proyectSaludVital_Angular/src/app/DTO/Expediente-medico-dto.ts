import { EntradaHistorialDTO } from "./paciente-ver-expedi";


export interface ExpedienteMedicoDTO {
  id: number;
  nombrePaciente: string;
  numeroIdentificacion: string;
  fechaNacimiento: string;
  entradasHistorial: EntradaHistorialDTO[];
}