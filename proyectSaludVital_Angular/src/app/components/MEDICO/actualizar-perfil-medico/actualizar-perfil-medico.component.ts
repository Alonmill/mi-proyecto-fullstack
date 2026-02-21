import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ObtenerMedicoDTO } from '../../../DTO/obtener-medico-dto';
import { MedicoService } from '../../../services/medico.service';

@Component({
  selector: 'app-actualizar-perfil-medico',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './actualizar-perfil-medico.component.html',
  styleUrl: './actualizar-perfil-medico.component.css'
})
export class ActualizarPerfilMedicoComponent implements OnInit {
  medico: ObtenerMedicoDTO | null = null;
  form: FormGroup;
  mensaje = '';
  mensajeError = false;

  readonly especialidades = [
    'CARDIOLOGIA',
    'PEDIATRIA',
    'DERMATOLOGIA',
    'GINECOLOGIA',
    'NEUROLOGIA'
  ];

  constructor(
    private medicoService: MedicoService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      apellido: ['', Validators.required],
      numeroLicencia: ['', Validators.required],
      telefono: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      especialidad: ['', Validators.required],
      tarifaConsulta: [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.medicoService.getPerfil().subscribe({
      next: (data) => {
        this.medico = data;
        this.form.patchValue({
          nombre: data.nombre,
          apellido: data.apellido,
          numeroLicencia: data.numeroLicencia,
          telefono: data.telefono,
          email: data.email,
          especialidad: data.especialidad,
          tarifaConsulta: data.tarifaConsulta
        });
      },
      error: () => {
        this.mensajeError = true;
        this.mensaje = '❌ No se pudo cargar el perfil para editar';
      }
    });
  }

  guardar(): void {
    this.mensaje = '';

    if (!this.medico || this.form.invalid) {
      this.form.markAllAsTouched();
      this.mensajeError = true;
      this.mensaje = '❌ Revisa los campos antes de guardar';
      return;
    }

    const payload = {
      ...this.form.value,
      estado: this.medico.estado,
      disponible: this.medico.disponible,
      usuarioId: this.medico.usuarioId,
      horarios: this.medico.horarios
    };

    this.medicoService.actualizarPerfil(payload).subscribe({
      next: () => {
        this.mensajeError = false;
        this.mensaje = '✅ Perfil actualizado correctamente';
        setTimeout(() => this.router.navigate(['/medico/perfil-medico']), 900);
      },
      error: (err) => {
        this.mensajeError = true;
        this.mensaje = err?.error?.message || '❌ No se pudo actualizar el perfil';
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/medico/perfil-medico']);
  }
}
