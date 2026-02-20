export interface ItemRecetaActualizarDTO {
  medicamentoId: number;
  dosis: string;
  frecuencia: string;
}

export interface ActualizarRecetaDTO {
  idReceta: number;
  items: ItemRecetaActualizarDTO[];
}