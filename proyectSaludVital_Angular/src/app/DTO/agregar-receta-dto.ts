export interface ItemRecetaAgregarDTO {
  medicamentoId: number;
  dosis: string;
  frecuencia: string;
}

export interface AgregarRecetaDTO {
  items: ItemRecetaAgregarDTO[];
}