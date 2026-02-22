export interface HorarioDTO {
  dia: string;
  horaInicio: string;
  horaFin: string;
}

export interface ObtenerMedicoDTO {
  id: number;
  nombre: string;
  apellido: string;
  numeroLicencia: string;
  telefono: string;
  email: string;
  especialidad: string;
  estado?: string;
  disponible?: boolean;
  tarifaConsulta: number;
  usuarioId: number;
  imagenUrl?: string;
  horarios: HorarioDTO[];
}
