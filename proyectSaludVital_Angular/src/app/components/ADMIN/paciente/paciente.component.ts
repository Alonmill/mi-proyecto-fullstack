import { Component } from '@angular/core';
import { PacienteConMostrar } from '../../../DTO/paciente-enfermedad-alergia';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { PacienteService } from '../../../services/paciente.service';
import { ObtenerPacienteDTO } from '../../../DTO/obtener-paciente-dto';
import { UsuarioAdminService } from '../../../services/usuario-admin.service';

@Component({
  selector: 'app-paciente',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './paciente.component.html',
  styleUrl: './paciente.component.css'
})
export class PacienteComponent {
  pacientes: PacienteConMostrar[] = [];
  form: FormGroup;
  editar = false;
  pacienteSeleccionadoId: number | null = null;
  mensajeError: string | null = null;
  emailBusquedaUsuario = '';

  constructor(
    private pacienteService: PacienteService,
    private usuarioAdminService: UsuarioAdminService,
    private fb: FormBuilder
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      numeroIdentificacion: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
      fechaNacimiento: ['', Validators.required],
      alergias: this.fb.array([]),
      enfermedades: this.fb.array([]),
      usuarioId: [null, [Validators.required, Validators.pattern(/^\d+$/)]]
    });
  }

  ngOnInit(): void {
    this.listarPacientes();
  }

  get alergias(): FormArray {
    return this.form.get('alergias') as FormArray;
  }

  get enfermedades(): FormArray {
    return this.form.get('enfermedades') as FormArray;
  }

  listarPacientes() {
    this.pacienteService.listar().subscribe((data) => {
      this.pacientes = data;
    });
  }

  buscarUsuarioPorEmail() {
    this.mensajeError = null;
    const email = this.emailBusquedaUsuario.trim();

    if (!email) {
      this.mensajeError = 'Ingresa un email para buscar el usuario.';
      return;
    }

    this.usuarioAdminService.buscarPorEmail(email).subscribe({
      next: (usuario) => {
        this.form.patchValue({ usuarioId: usuario.id });
      },
      error: (err) => {
        console.error(err);
        this.mensajeError = err.error?.message || 'No se encontró un usuario con ese email.';
      }
    });
  }

  agregarAlergia() {
    this.alergias.push(this.fb.group({ nombre: ['', Validators.required] }));
  }

  agregarEnfermedad() {
    this.enfermedades.push(this.fb.group({ nombre: ['', Validators.required] }));
  }

  eliminarAlergia(index: number) {
    this.alergias.removeAt(index);
  }

  eliminarEnfermedad(index: number) {
    this.enfermedades.removeAt(index);
  }

  editarPaciente(paciente: ObtenerPacienteDTO) {
    this.editar = true;
    this.pacienteSeleccionadoId = paciente.id || null;

    this.form.patchValue({
      nombre: paciente.nombre,
      numeroIdentificacion: paciente.numeroIdentificacion,
      fechaNacimiento: paciente.fechaNacimiento,
      usuarioId: paciente.usuarioId
    });

    this.alergias.clear();
    paciente.alergias.forEach((a) => {
      this.alergias.push(this.fb.group({ nombre: [a, Validators.required] }));
    });

    this.enfermedades.clear();
    paciente.enfermedades.forEach((e) => {
      this.enfermedades.push(this.fb.group({ nombre: [e, Validators.required] }));
    });
  }

  cancelarEdicion() {
    this.editar = false;
    this.pacienteSeleccionadoId = null;
    this.emailBusquedaUsuario = '';
    this.form.reset();
    this.alergias.clear();
    this.enfermedades.clear();
    this.mensajeError = null;
  }

  guardar() {
    if (this.form.invalid) return;

    this.mensajeError = null;

    const pacienteData = {
      ...this.form.value,
      alergias: this.form.value.alergias.map((a: any) => a.nombre),
      enfermedades: this.form.value.enfermedades.map((e: any) => e.nombre)
    };

    if (this.editar && this.pacienteSeleccionadoId) {
      this.pacienteService.actualizar(this.pacienteSeleccionadoId, pacienteData).subscribe({
        next: () => {
          this.listarPacientes();
          this.cancelarEdicion();
        },
        error: (err) => {
          console.error(err);
          this.mensajeError = err.error?.message || 'Error al actualizar paciente';
        }
      });
    } else {
      this.pacienteService.agregar(pacienteData).subscribe({
        next: () => {
          this.listarPacientes();
          this.cancelarEdicion();
        },
        error: (err) => {
          console.error(err);
          this.mensajeError = err.error?.message || 'Error al agregar paciente';
        }
      });
    }
  }
}
