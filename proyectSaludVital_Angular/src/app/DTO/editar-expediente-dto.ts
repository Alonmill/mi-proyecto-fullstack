export interface EditarExpedienteDTO {
  id: number;
  nombrePaciente: string;
  numeroIdentificacion: string;
  fechaNacimiento: string; // LocalDate -> string ISO en Angular
}