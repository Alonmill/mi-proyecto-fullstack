export interface AlergiaDTO{
     nombre: string;
}

export interface EnfermedadDTO{
     nombre: string;
}

export interface ObtenerPacienteDTO{
  id: number;
  nombre: string;
  numeroIdentificacion: string;
  fechaNacimiento: string; // LocalDate se representa como string en JSON (formato: 'YYYY-MM-DD')
  alergias: string[];
  enfermedades: string[];
  usuarioId: number; // Long en Java se mapea como number en TypeScript
  imagenUrl?: string;
}