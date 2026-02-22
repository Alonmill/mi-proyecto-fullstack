import { Component, OnInit } from '@angular/core';

import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { CitaService } from '../../../services/cita.service';

@Component({
  selector: 'app-mis-citas-paciente',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './mis-citas-paciente.component.html',
  styleUrl: './mis-citas-paciente.component.css'
})
export class MisCitasPacienteComponent implements OnInit {
  citas: any[] = []; // <-- agregamos la propiedad para la tabla
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
    this.citaService.misCitas().subscribe({
      next: data => {
        this.citas = data;
        this.paginaActual = 1;
      },
      error: err => console.error('Error al obtener mis citas', err)
    });
  }
  obtenerClaseEstado(estado?: string): string {
    const estadoNormalizado = (estado || '').toUpperCase();

    if (estadoNormalizado === 'ATENDIDA') {
      return 'estado-atendida';
    }

    if (estadoNormalizado === 'PROGRAMADA' || estadoNormalizado === 'ACTIVA') {
      return 'estado-programada';
    }

    if (estadoNormalizado === 'CANCELADA') {
      return 'estado-cancelada';
    }

    if (estadoNormalizado === 'VENCIDA') {
      return 'estado-vencida';
    }

    return 'estado-default';
  }
}
