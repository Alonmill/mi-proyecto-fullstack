import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { EstadoReceta } from '../../../DTO/obtener-receta-dto';
import { RecetaService } from '../../../services/receta.service';

@Component({
  selector: 'app-listado-receta',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './listado-receta.component.html',
  styleUrl: './listado-receta.component.css'
})
export class ListadoRecetaComponent {
  recetas: any[] = [];
  cargando = true;
  paginaActual = 1;
  readonly registrosPorPagina = 10;

  get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.recetas.length / this.registrosPorPagina));
  }

  get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i + 1);
  }

  get recetasPaginadas(): any[] {
    const inicio = (this.paginaActual - 1) * this.registrosPorPagina;
    return this.recetas.slice(inicio, inicio + this.registrosPorPagina);
  }

  irPagina(pagina: number): void {
    if (pagina < 1 || pagina > this.totalPaginas) return;
    this.paginaActual = pagina;
  }

  paginaAnterior(): void {
    this.irPagina(this.paginaActual - 1);
  }

  paginaSiguiente(): void {
    this.irPagina(this.paginaActual + 1);
  }

  constructor(private recetaService: RecetaService) {}

  ngOnInit(): void {
    this.listarRecetas();
  }

  listarRecetas(): void {
    this.cargando = true;
    this.recetaService.listar().subscribe({
      next: data => {
        this.recetas = data;
        this.paginaActual = 1;
        this.cargando = false;
      },
      error: err => {
        console.error('Error al listar recetas', err);
        this.cargando = false;
      }
    });
  }

  obtenerClaseEstado(estado: EstadoReceta): string {
    const clases: Record<EstadoReceta, string> = {
      BORRADOR: 'badge-borrador',
      EMITIDA: 'badge-emitida',
      DISPENSADA: 'badge-dispensada',
      ANULADA: 'badge-anulada',
      VENCIDA: 'badge-vencida'
    };
    return clases[estado];
  }
}
