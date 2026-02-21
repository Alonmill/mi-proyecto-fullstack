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

  constructor(private recetaService: RecetaService) {}

  ngOnInit(): void {
    this.listarRecetas();
  }

  listarRecetas(): void {
    this.cargando = true;
    this.recetaService.listar().subscribe({
      next: data => {
        this.recetas = data;
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
