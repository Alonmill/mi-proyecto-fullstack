import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CitaService } from '../../../services/cita.service';
import { CommonModule } from '@angular/common';
import { CitaObtenidaDTO } from '../../../DTO/cita-obtenida-dto';

@Component({
  selector: 'app-actualizar-cita',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './actualizar-cita.component.html',
  styleUrl: './actualizar-cita.component.css'
})
export class ActualizarCitaComponent implements OnInit {
  citas: CitaObtenidaDTO[] = [];
  formulario: FormGroup;
  citaSeleccionadaId: number | null = null;
  mensaje: string = '';

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

  cargarCitas() {
    this.citaService.listarProgramadas().subscribe(res => {
      this.citas = res;
    });
  }

  seleccionarCita(cita: CitaObtenidaDTO) {
    this.citaSeleccionadaId = cita.id!;
    this.formulario.patchValue({
      idMedico: cita.medicoId, // si necesitas el id del medico, ajusta aquí
      fecha: cita.fecha,
      hora: cita.hora,
      motivo: cita.motivo
    });
  }

  onSubmit() {
    if (this.formulario.invalid || this.citaSeleccionadaId === null) return;

    this.citaService.actualizar(this.citaSeleccionadaId, this.formulario.value)
      .subscribe({
        next: res => {
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
