
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { CommonModule } from '@angular/common';
import { CitaService } from '../../../services/cita.service';
import { CrearCitaDTO } from '../../../DTO/crear-cita-dto';
import { MedicoConMostrar } from '../../../DTO/medico-horario';
import { MedicoService } from '../../../services/medico.service';

@Component({
  selector: 'app-crear-cita',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './crear-cita.component.html',
  styleUrl: './crear-cita.component.css'
})

export class CrearCitaComponent implements OnInit {
  medicos: MedicoConMostrar[] = [];
  form: FormGroup;
  mensaje: string = '';
  fechaMin: string = '';

  constructor(
    private fb: FormBuilder,
    private citaService: CitaService,
    private medicoService: MedicoService
  ) {
    this.form = this.fb.group({
      idMedico: [null, Validators.required],
      fecha: ['', Validators.required],
      hora: ['', Validators.required],
      motivo: ['', Validators.required]
    });
  }


  ngOnInit(): void {
  this.fechaMin = new Date().toISOString().split('T')[0];
  this.fechaMin = new Date().toISOString().split('T')[0];
 
     this.medicoService.listar().subscribe({
       next: data => {
         this.medicos = data;
       },
       error: err => {
         console.error('Error cargando médicos', err);
         this.mensaje = '❌ No se pudo cargar el listado de médicos';
       }
     });
   }
 
   get medicoSeleccionado(): MedicoConMostrar | undefined {
     const medicoId = Number(this.form.get('idMedico')?.value);
     return this.medicos.find(m => m.id === medicoId);
   }
 
   private obtenerMensajeError(err: any): string {
     const detalle = err?.error;
 
     if (typeof detalle === 'string' && detalle.trim()) {
       return detalle;
    }


 if (typeof detalle?.message === 'string' && detalle.message.trim()) {
      return detalle.message;
 }

    if (typeof detalle?.error === 'string' && detalle.error.trim()) {
      return detalle.error;
    }

    if (Array.isArray(detalle?.errors) && detalle.errors.length > 0) {
      const primerError = detalle.errors[0];
      if (typeof primerError === 'string') {
        return primerError;
      }
      if (typeof primerError?.defaultMessage === 'string') {
        return primerError.defaultMessage;
      }
      if (typeof primerError?.message === 'string') {
        return primerError.message;
      }
    }

    return 'Error desconocido al crear cita';
  }

 private normalizarMensajeCita(detalle: string): string {
    if (detalle.includes('3 citas')) {
      return 'Ya tienes el máximo de 3 citas para ese día.';
    }

    if (detalle.includes('horario ya está ocupado')) {
      return 'Ese horario ya está ocupado. Elige otro horario o médico.';
    }

    if (detalle.includes('fuera del horario de atención')) {
      return 'La hora seleccionada está fuera del horario de atención del médico.';
    }

    return detalle;
  }

  onSubmit() {
    if (!this.form.valid) {
      this.form.markAllAsTouched();
      return;
    }

    const dto: CrearCitaDTO = this.form.value;
    this.citaService.crear(dto).subscribe({
      next: () => {
        this.mensaje = '✅ Cita creada correctamente';
        this.form.reset();
      },
      error: err => {
        console.error('Error al crear cita', err);

        const detalle = this.obtenerMensajeError(err);
        const mensajeUsuario = this.normalizarMensajeCita(detalle);
        this.mensaje = `❌ ${mensajeUsuario}`;
      }
    });
    }
}
