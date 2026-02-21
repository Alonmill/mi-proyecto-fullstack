import { Component, OnInit } from '@angular/core';
import { CitaService } from '../../../services/cita.service';
import { CitaObtenidaDTO } from '../../../DTO/cita-obtenida-dto';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-actualizar-cita-admin',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './actualizar-cita-admin.component.html',
  styleUrl: './actualizar-cita-admin.component.css'
})
export class ActualizarCitaAdminComponent implements OnInit {
  citas: CitaObtenidaDTO[] = [];
  formulario: FormGroup;
  citaSeleccionadaId: number | null = null;
  mensajeError: string | null = null;
  mensajeInfo: string | null = null;

  constructor(private citaService: CitaService, private fb: FormBuilder) {
    this.formulario = this.fb.group({
      idMedico: [null, Validators.required],
      fecha: [null, Validators.required],
      hora: [null, Validators.required],
      motivo: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.cargarCitas();
  }

  private tieneDosHorasAnticipacion(cita: CitaObtenidaDTO): boolean {
    if (!cita.fecha || !cita.hora) return false;
    const fechaHora = new Date(`${cita.fecha}T${String(cita.hora).slice(0, 5)}:00`);
    return fechaHora.getTime() - Date.now() >= 2 * 60 * 60 * 1000;
  }

  cargarCitas() {
    this.citaService.listarProgramadas().subscribe((res) => {
      this.citas = (res || []).filter((cita) => this.tieneDosHorasAnticipacion(cita));

      if (this.citas.length === 0) {
        this.mensajeInfo = 'No hay citas disponibles para actualizar con al menos 2 horas de anticipación.';
      } else {
        this.mensajeInfo = null;
      }
    });
  }

  seleccionarCita(cita: CitaObtenidaDTO) {
    this.citaSeleccionadaId = cita.id!;
    this.formulario.patchValue({
      idMedico: cita.medicoId,
      fecha: cita.fecha,
      hora: cita.hora,
      motivo: cita.motivo
    });
  }

  onSubmit() {
    if (this.formulario.invalid || this.citaSeleccionadaId === null) return;

    this.mensajeError = null;

    this.citaService.actualizar(this.citaSeleccionadaId, this.formulario.value).subscribe({
      next: () => {
        alert('✅ Cita actualizada correctamente');
        this.formulario.reset();
        this.citaSeleccionadaId = null;
        this.cargarCitas();
      },
      error: (err) => {
        console.error(err);

        if (err.error && typeof err.error === 'string') {
          if (err.error.includes('2 horas')) {
            this.mensajeError = '⏰ La cita solo se puede actualizar con al menos 2 horas de anticipación.';
          } else if (err.error.includes('3 citas')) {
            this.mensajeError = '⚠️ El paciente ya tiene 3 citas en el mismo día.';
          } else if (err.error.includes('horario ya está ocupado')) {
            this.mensajeError = '📌 El horario ya está ocupado por otro médico.';
          } else {
            this.mensajeError = '❌ Error al actualizar la cita.';
          }
        } else {
          this.mensajeError = '❌ Error al actualizar la cita.';
        }
      }
    });
  }
}
