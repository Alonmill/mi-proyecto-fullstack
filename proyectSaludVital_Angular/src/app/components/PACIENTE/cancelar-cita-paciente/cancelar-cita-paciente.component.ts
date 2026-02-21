import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { CitaService } from '../../../services/cita.service';
import { CitaObtenidaDTO } from '../../../DTO/cita-obtenida-dto';

@Component({
  selector: 'app-cancelar-cita-paciente',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './cancelar-cita-paciente.component.html',
  styleUrl: './cancelar-cita-paciente.component.css'
})
export class CancelarCitaPacienteComponent implements OnInit {
  citas: CitaObtenidaDTO[] = [];
  mensaje: string = '';

  constructor(private citaService: CitaService) {}

  ngOnInit(): void {
    this.cargarCitas();
  }

  private fechaHoraConAnticipacionValida(fecha: string, hora: string): boolean {
    const fechaHoraCita = new Date(`${fecha}T${hora}`);
    if (Number.isNaN(fechaHoraCita.getTime())) {
      return false;
    }

   const ahoraMasDosHoras = new Date(Date.now() + 2 * 60 * 60 * 1000);
    return fechaHoraCita >= ahoraMasDosHoras;
  }
  private citaCancelable(cita: CitaObtenidaDTO): boolean {
    return cita.estado === 'PROGRAMADA' &&
      this.fechaHoraConAnticipacionValida(cita.fecha, cita.hora);
  }

  cargarCitas() {
    this.citaService.misCitas().subscribe({
      next: (data) => {
        this.citas = data.filter(cita => this.citaCancelable(cita));

    if (this.citas.length === 0) {
          this.mensaje = 'ℹ️ No tienes citas programadas con al menos 2 horas de anticipación para cancelar.';
        } else if (this.mensaje.startsWith('ℹ️')) {
          this.mensaje = '';
        }
      },
      error: (err) => {
        console.error('Error al cargar citas', err);
        this.mensaje = '❌ No se pudieron cargar las citas para cancelar';
      }
    });
  }

  cancelarCita(citaId: number) {
    this.citaService.cancelar(citaId).subscribe({
      next: () => {
        this.mensaje = '✅ Cita cancelada correctamente';
       this.cargarCitas();
      },
      error: err => {
        console.error('Error al cancelar cita', err);
        const detalle = typeof err?.error === 'string'
          ? err.error
          : (err?.error?.message || 'No se pudo cancelar la cita');
        this.mensaje = `❌ ${detalle}`;
      }
    });
  }
}

