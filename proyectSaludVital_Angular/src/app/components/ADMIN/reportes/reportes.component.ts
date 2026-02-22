import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ReporteTablaDTO } from '../../../DTO/reporte-tabla-dto';
import { ReportesService } from '../../../services/reportes.service';

interface ReporteCard {
  key: string;
  endpoint: string;
  descripcion: string;
  requiresDate?: boolean;
}

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reportes.component.html',
  styleUrl: './reportes.component.css'
})
export class ReportesComponent {
  readonly reportes: ReporteCard[] = [
    { key: 'Citas', endpoint: 'citas', descripcion: 'Agenda, estado y cobro de consultas.' },
    { key: 'Recetas', endpoint: 'recetas', descripcion: 'Recetas emitidas y estado actual.' },
    { key: 'Expedientes', endpoint: 'expedientes', descripcion: 'Historial clínico con detalle médico.' },
    { key: 'Pacientes', endpoint: 'pacientes', descripcion: 'Listado general de pacientes registrados.' },
    { key: 'Médicos', endpoint: 'medicos', descripcion: 'Médicos, especialidad y tarifa.' },
    { key: 'Medicamentos', endpoint: 'medicamentos', descripcion: 'Catálogo de medicamentos disponibles.' },
    { key: 'Pacientes por Día', endpoint: 'pacientes-por-dia', descripcion: 'Consultas del día seleccionado.', requiresDate: true }
  ];

  cargando = false;
  error = '';
  reporteActual: ReporteTablaDTO | null = null;
  reporteSeleccionado = '';
  fechaPacientesDia = new Date().toISOString().split('T')[0];

  constructor(private reportesService: ReportesService) {}

  generar(card: ReporteCard): void {
    this.error = '';
    this.cargando = true;
    this.reporteSeleccionado = card.key;

    this.reportesService.obtenerData(card.endpoint, this.getFecha(card)).subscribe({
      next: data => {
        this.reporteActual = data;
        this.cargando = false;
      },
      error: err => {
        this.error = err?.error?.message || 'No se pudo generar el reporte.';
        this.cargando = false;
      }
    });
  }

  exportar(card: ReporteCard): void {
    this.error = '';
    this.reportesService.exportarPdf(card.endpoint, this.getFecha(card)).subscribe({
      next: blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${card.endpoint}-${new Date().toISOString().slice(0, 10)}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: err => {
        this.error = err?.error?.message || 'No se pudo exportar el PDF.';
      }
    });
  }

  private getFecha(card: ReporteCard): string | undefined {
    return card.requiresDate ? this.fechaPacientesDia : undefined;
  }
}
