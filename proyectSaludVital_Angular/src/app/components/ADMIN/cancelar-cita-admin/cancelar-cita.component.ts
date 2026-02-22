import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { CitaService } from '../../../services/cita.service';

@Component({
  selector: 'app-cancelar-cita',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './cancelar-cita.component.html',
  styleUrl: './cancelar-cita.component.css'
})
export class CancelarCitaComponent implements OnInit {
  citas: any[] = [];
  mensaje = '';
  mensajeInfo = '';
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
    this.cargarCitas();
  }

  private tieneDosHorasAnticipacion(cita: any): boolean {
    if (!cita?.fecha || !cita?.hora) return false;
    const fechaHora = new Date(`${cita.fecha}T${String(cita.hora).slice(0, 5)}:00`);
    return fechaHora.getTime() - Date.now() >= 2 * 60 * 60 * 1000;
  }

  cargarCitas() {
    this.citaService.listar().subscribe({
      next: (data) => {
        this.citas = (data || []).filter((cita: any) => this.tieneDosHorasAnticipacion(cita));
        this.paginaActual = 1;
        this.mensajeInfo =
          this.citas.length === 0
            ? 'No hay citas disponibles para cancelar con al menos 2 horas de anticipación.'
            : '';
      },
      error: (err) => console.error('Error al cargar citas', err)
    });
  }

  cancelarCita(citaId: number) {
    const cita = this.citas.find((c) => c.id === citaId);
    if (!cita) return;

    if (cita.estado === 'ATENDIDA') {
      this.mensaje = '❌ No se puede cancelar una cita ATENDIDA';
      return;
    }

    if (cita.estado === 'PROGRAMADA') {
      const fechaHoraCita = new Date(cita.fecha + 'T' + cita.hora);
      const ahora = new Date();
      const diffHoras = (fechaHoraCita.getTime() - ahora.getTime()) / 1000 / 3600;

      if (diffHoras < 2) {
        this.mensaje = '⚠️ La cita solo se puede cancelar con al menos 2 horas de anticipación';
        return;
      }
    }

    this.citaService.cancelar(citaId).subscribe({
      next: () => {
        this.mensaje = '✅ Cita cancelada correctamente';
        this.citas = this.citas.map((c) => (c.id === citaId ? { ...c, estado: 'CANCELADA' } : c));
      },
      error: (err) => console.error('Error al cancelar cita', err)
    });
  }
}
