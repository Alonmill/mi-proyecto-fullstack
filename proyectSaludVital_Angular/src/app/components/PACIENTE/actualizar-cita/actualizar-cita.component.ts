import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CitaService } from '../../../services/cita.service';
import { CommonModule } from '@angular/common';
import { CitaObtenidaDTO } from '../../../DTO/cita-obtenida-dto';

import { MedicoConMostrar } from '../../../DTO/medico-horario';
import { MedicoService } from '../../../services/medico.service';
@Component({
  selector: 'app-actualizar-cita',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './actualizar-cita.component.html',
  styleUrl: './actualizar-cita.component.css'
})
export class ActualizarCitaComponent implements OnInit {
  citas: CitaObtenidaDTO[] = [];
  medicos: MedicoConMostrar[] = [];
  formulario: FormGroup;
  citaSeleccionadaId: number | null = null;
  mensaje: string = '';

  constructor(
    private citaService: CitaService,
    private medicoService: MedicoService,
    private fb: FormBuilder
  ) {
    this.formulario = this.fb.group({
      idMedico: [null, Validators.required],
      fecha: [null, Validators.required],
      hora: [null, Validators.required],
      motivo: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.cargarCitas();
     this.cargarMedicos();
  }

  get medicoSeleccionado(): MedicoConMostrar | undefined {
    const medicoId = Number(this.formulario.get('idMedico')?.value);
    return this.medicos.find(m => m.id === medicoId);
  }

  private fechaHoraConAnticipacionValida(fecha: string, hora: string): boolean {
    const fechaHoraCita = new Date(`${fecha}T${hora}`);
    if (Number.isNaN(fechaHoraCita.getTime())) {
      return false;
    }

    const ahoraMasDosHoras = new Date(Date.now() + 2 * 60 * 60 * 1000);
    return fechaHoraCita >= ahoraMasDosHoras;
  }

  cargarMedicos() {
    this.medicoService.listar().subscribe({
      next: data => {
        this.medicos = data;
      },
      error: err => {
        console.error(err);
        this.mensaje = '❌ No se pudieron cargar los médicos';
      }
    });
  }



  cargarCitas() {
     this.citaService.listarProgramadas().subscribe({
      next: res => {
        this.citas = res.filter(cita =>
          this.fechaHoraConAnticipacionValida(cita.fecha, cita.hora)
        );

        if (this.citas.length === 0) {
          this.mensaje = 'ℹ️ No tienes citas programadas con al menos 2 horas de anticipación para actualizar.';
          this.citaSeleccionadaId = null;
        } else if (this.mensaje.startsWith('ℹ️')) {
          this.mensaje = '';
        }
      },
      error: err => {
        console.error(err);
        this.mensaje = '❌ No se pudieron cargar las citas para actualizar';
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

    this.citaService.actualizar(this.citaSeleccionadaId, this.formulario.value)
      .subscribe({
        next: () => {
          this.mensaje = '✅ Cita actualizada correctamente';
          this.formulario.reset();
          this.citaSeleccionadaId = null;
          this.cargarCitas();
        },
        error: err => {
          console.error(err);

          let mensaje = '❌ Error al actualizar la cita';

          if (err.error && typeof err.error === 'string') {
            mensaje = '❌ ' + err.error;
          } else if (err.error && err.error.message) {
            mensaje = '❌ ' + err.error.message;
          }

          if (mensaje.includes('2 horas')) {
            mensaje = '⏰ La cita solo se puede actualizar con al menos 2 horas de anticipación';
          }

          this.mensaje = mensaje;
        }
      });
  }
}
