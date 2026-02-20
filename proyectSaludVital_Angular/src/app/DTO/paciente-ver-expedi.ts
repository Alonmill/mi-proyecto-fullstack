export interface EntradaHistorialDTO{
    id: number;
    fecha: string;
    hora: string;
    diagnostico: string;
    tratamiento: string;
    nombreMedico: string;
    nombrePaciente: string;
    estadoCita: string;

}

export interface ObtenerExpedienteMedicoDTO {

    id: number;
  nombrePaciente: string;
  numeroIdentificacion: string;
  fechaNacimiento: string;
  entradasHistorial : EntradaHistorialDTO[];

}