export interface ItemRecetaObtenerDTO {
  medicamentoId: number;
  dosis: string;
  frecuencia: string;
  medicamentoNombre: string;
}

export interface ObtenerRecetaDTO {
  id: number;
  fechaEmision: string;
  fechaCaducidad: string;
  medicoNombre: string;
  pacienteNombre: string;
  items: ItemRecetaObtenerDTO[];
}
