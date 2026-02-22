import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { CitaService } from '../../../services/cita.service';

@Component({
  selector: 'app-listado-citas',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './listado-citas.component.html',
  styleUrl: './listado-citas.component.css'
})
export class ListadoCitasComponent implements OnInit {
  citas: any[] = [];
  paginaActual = 1;
  readonly registrosPorPagina = 10;

  get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.citas.length / this.registrosPorPagina));
  }

  get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i + 1);
  }

  get citasPaginadas(): any[] {
    const inicio = (this.paginaActual - 1) * this.registrosPorPagina;
    return this.citas.slice(inicio, inicio + this.registrosPorPagina);
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

  constructor(private citaService: CitaService) {}

  ngOnInit(): void {
    this.citaService.listar().subscribe({
      next: data => {
        this.citas = data;
        this.paginaActual = 1;
      },
      error: err => console.error('Error al obtener listado de citas', err)
    });
  }

  obtenerClaseEstado(estado: string): string {
    const clases: Record<string, string> = {
      CANCELADA: 'estado-cancelada',
      VENCIDA: 'estado-vencida',
      ATENDIDA: 'estado-atendida',
      PROGRAMADA: 'estado-programada'
    };

    return clases[estado] || 'estado-default';
  }
}