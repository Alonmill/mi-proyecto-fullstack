export interface CrearCitaDTO {
  idPaciente: number;
  idMedico: number;
  fecha: string; // "yyyy-MM-dd"
  hora: string;  // "HH:mm:ss"
  motivo: string;
}