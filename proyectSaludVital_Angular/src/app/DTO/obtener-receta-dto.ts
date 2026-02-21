export interface ItemRecetaObtenerDTO {
  medicamentoId: number;
  dosis: string;
  frecuencia: string;
  medicamentoNombre: string;
}

export type EstadoReceta = 'BORRADOR' | 'EMITIDA' | 'DISPENSADA' | 'ANULADA' | 'VENCIDA';

export interface ObtenerRecetaDTO {
  id: number;
  fechaEmision: string;
  fechaCaducidad: string;
  medicoNombre: string;
  pacienteNombre: string;
  estado: EstadoReceta;
  items: ItemRecetaObtenerDTO[];
}
