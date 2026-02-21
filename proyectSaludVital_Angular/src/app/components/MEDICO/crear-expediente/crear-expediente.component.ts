import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../services/api.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-crear-expediente',
  standalone: true,
  imports: [CommonModule,ReactiveFormsModule],
  templateUrl: './crear-expediente.component.html',
  styleUrl: './crear-expediente.component.css'
})
export class CrearExpedienteComponent {
  formulario: FormGroup;
  mensaje: string = '';
  tipoMensaje: string = '';

  constructor(private fb: FormBuilder, private api: ApiService, private router: Router) {
   this.formulario = this.fb.group({
  idPaciente: ['', Validators.required],
  citaId: ['', Validators.required],
  diagnostico: ['', [Validators.required, Validators.maxLength(200)]],
  tratamiento: ['', [Validators.required, Validators.maxLength(200)]]
});
  }

  crear() {
  if (this.formulario.invalid) return;

  const { idPaciente, ...data } = this.formulario.value;

  this.api.post<any>(`expedientes/entrada/nueva/${idPaciente}`, data)
    .subscribe({
      next: res => {
        this.mensaje = 'Expediente creado correctamente';
        this.tipoMensaje = 'exito';
        this.formulario.reset();
        this.router.navigate(['/medico/receta-agregar', idPaciente]);
      },
      error: err => {
        console.error(err);

        // Capturamos el mensaje del backend si existe
        if (err.error && err.error.message) {
          this.mensaje = err.error.message;
        } else if (err.error) {
          this.mensaje = JSON.stringify(err.error); // para ver detalles del backend
        } else {
          this.mensaje = 'Error al crear expediente';
        }

        this.tipoMensaje = 'error';
      }
    });
}

}
