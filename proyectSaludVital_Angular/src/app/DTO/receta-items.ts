
import { ObtenerRecetaDTO } from "./obtener-receta-dto";


export interface RecetaConMostrar extends ObtenerRecetaDTO {
  showItems: boolean;
}