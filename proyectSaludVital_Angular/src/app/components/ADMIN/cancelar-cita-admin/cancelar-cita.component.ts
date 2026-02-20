import { Component, Input, OnInit } from '@angular/core';

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
  citas: any[] = []; // <-- agregamos la propiedad para la tabla
  mensaje: string = '';

  constructor(private citaService: CitaService) {}

  ngOnInit(): void {
    this.cargarCitas();
  }

  cargarCitas() {
    this.citaService.listar().subscribe({
      next: (data) => this.citas = data,
      error: (err) => console.error('Error al cargar citas', err)
    });
  }

  cancelarCita(citaId: number) {
  const cita = this.citas.find(c => c.id === citaId);
  if (!cita) return;

  if (cita.estado === 'COMPLETADA') {
    this.mensaje = '❌ No se puede cancelar una cita COMPLETADA';
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

  // si pasa las validaciones, llama al servicio
  if (citaId) {
    this.citaService.cancelar(citaId).subscribe({
      next: () => {
        this.mensaje = '✅ Cita cancelada correctamente';
        // actualizar la tabla quitando o actualizando el estado
        this.citas = this.citas.map(c => 
          c.id === citaId ? { ...c, estado: 'CANCELADA' } : c
        );
      },
      error: err => console.error('Error al cancelar cita', err)
    });
  }
}
}