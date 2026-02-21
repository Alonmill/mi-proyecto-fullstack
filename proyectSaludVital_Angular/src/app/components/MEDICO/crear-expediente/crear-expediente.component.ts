import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../services/api.service';
import { Router } from '@angular/router';

interface CitaAtendibleDTO {
  id: number;
  fecha: string;
  hora: string;
  pacienteId: number;
  pacienteNombre: string;
}

@Component({
  selector: 'app-crear-expediente',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './crear-expediente.component.html',
  styleUrl: './crear-expediente.component.css'
})
export class CrearExpedienteComponent implements OnInit {
  formulario: FormGroup;
  mensaje = '';
  tipoMensaje = '';
  cargandoCitas = false;

  citasAtendibles: CitaAtendibleDTO[] = [];
  citasFiltradas: CitaAtendibleDTO[] = [];
  pacientesDisponibles: Array<{ id: number; nombre: string }> = [];

  constructor(private fb: FormBuilder, private api: ApiService, private router: Router) {
    this.formulario = this.fb.group({
      idPaciente: ['', Validators.required],
      citaId: ['', Validators.required],
      diagnostico: ['', [Validators.required, Validators.maxLength(200)]],
      tratamiento: ['', [Validators.required, Validators.maxLength(200)]]
    });
  }

  ngOnInit(): void {
    this.cargarCitasAtendibles();

    this.formulario.get('idPaciente')?.valueChanges.subscribe((idPaciente) => {
      this.filtrarCitasPorPaciente(Number(idPaciente));
    });

    this.formulario.get('citaId')?.valueChanges.subscribe((citaId) => {
      const cita = this.citasAtendibles.find(c => c.id === Number(citaId));
      if (cita && this.formulario.get('idPaciente')?.value !== cita.pacienteId) {
        this.formulario.patchValue({ idPaciente: cita.pacienteId }, { emitEvent: false });
        this.filtrarCitasPorPaciente(cita.pacienteId);
      }
    });
  }

  private cargarCitasAtendibles() {
    this.cargandoCitas = true;
    this.api.get<CitaAtendibleDTO[]>('citas/atendibles').subscribe({
      next: (citas) => {
        this.citasAtendibles = citas;
        this.pacientesDisponibles = this.mapearPacientes(citas);
        this.citasFiltradas = citas;
        this.cargandoCitas = false;
      },
      error: (err) => {
        console.error(err);
        this.mensaje = 'No se pudieron cargar citas atendibles en este momento.';
        this.tipoMensaje = 'error';
        this.cargandoCitas = false;
      }
    });
  }

  private mapearPacientes(citas: CitaAtendibleDTO[]): Array<{ id: number; nombre: string }> {
    const vistos = new Map<number, string>();
    citas.forEach(c => vistos.set(c.pacienteId, c.pacienteNombre));
    return Array.from(vistos.entries()).map(([id, nombre]) => ({ id, nombre }));
  }

  private filtrarCitasPorPaciente(idPaciente: number) {
    if (!idPaciente) {
      this.citasFiltradas = this.citasAtendibles;
      return;
    }

    this.citasFiltradas = this.citasAtendibles.filter(c => c.pacienteId === idPaciente);

    const citaActual = Number(this.formulario.get('citaId')?.value);
    if (citaActual && !this.citasFiltradas.some(c => c.id === citaActual)) {
      this.formulario.patchValue({ citaId: '' }, { emitEvent: false });
    }
  }

  formatearCita(cita: CitaAtendibleDTO): string {
    return `${cita.pacienteNombre} - ${cita.fecha} - ${cita.hora}`;
  }

  crear() {
    if (this.formulario.invalid) return;

    const { idPaciente, ...data } = this.formulario.value;

    this.api.post<any>(`expedientes/entrada/nueva/${idPaciente}`, data)
      .subscribe({
        next: () => {
          this.mensaje = 'Expediente creado correctamente';
          this.tipoMensaje = 'exito';
          this.formulario.patchValue({ diagnostico: '', tratamiento: '' });
          this.router.navigate(['/medico/receta-agregar', idPaciente]);
        },
        error: err => {
          console.error(err);
          if (err.error && err.error.message) {
            this.mensaje = err.error.message;
          } else if (err.error) {
            this.mensaje = JSON.stringify(err.error);
          } else {
            this.mensaje = 'Error al crear expediente';
          }
          this.tipoMensaje = 'error';
          this.cargarCitasAtendibles();
        }
      });
  }
}
