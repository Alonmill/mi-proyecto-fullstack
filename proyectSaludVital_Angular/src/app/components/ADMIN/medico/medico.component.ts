import { Component, OnInit } from '@angular/core';
import { MedicoService } from '../../../services/medico.service';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ObtenerMedicoDTO } from '../../../DTO/obtener-medico-dto';
import { MedicoConMostrar } from '../../../DTO/medico-horario';
import { UsuarioAdminService } from '../../../services/usuario-admin.service';

@Component({
  selector: 'app-medico',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './medico.component.html',
  styleUrl: './medico.component.css'
})
export class MedicoComponent implements OnInit {
  medicos: MedicoConMostrar[] = [];
  form: FormGroup;
  editar = false;
  medicoSeleccionadoId: number | null = null;
  mensajeError: string | null = null;
  emailBusquedaUsuario = '';

  constructor(
    private medicoService: MedicoService,
    private usuarioAdminService: UsuarioAdminService,
    private fb: FormBuilder
  ) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      apellido: ['', Validators.required],
      numeroLicencia: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(10)]],
      telefono: ['', [Validators.required, Validators.pattern(/^[0-9]{9}$/)]],
      email: ['', [Validators.required, Validators.email]],
      especialidad: ['', Validators.required],
      tarifaConsulta: [0, Validators.required],
      usuarioId: [null, Validators.required],
      disponible: [null, Validators.required],
      estado: ['ACTIVO', Validators.required],
      horarios: this.fb.array([])
    });
  }

  ngOnInit(): void {
    this.listarMedicos();
  }

  get horarios(): FormArray {
    return this.form.get('horarios') as FormArray;
  }

  listarMedicos() {
    this.medicoService.listar().subscribe((data) => {
      this.medicos = data;
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

  agregarHorario() {
    this.horarios.push(
      this.fb.group({
        dia: ['', Validators.required],
        horaInicio: ['', Validators.required],
        horaFin: ['', Validators.required]
      })
    );
  }

  eliminarHorario(index: number) {
    this.horarios.removeAt(index);
  }

  editarMedico(medico: ObtenerMedicoDTO) {
    this.editar = true;
    this.medicoSeleccionadoId = medico.id || null;

    this.form.patchValue({
      nombre: medico.nombre,
      apellido: medico.apellido,
      numeroLicencia: medico.numeroLicencia,
      telefono: medico.telefono,
      email: medico.email,
      especialidad: medico.especialidad,
      tarifaConsulta: medico.tarifaConsulta,
      usuarioId: medico.usuarioId,
      estado: medico.estado,
      disponible: medico.disponible
    });

    this.horarios.clear();
    medico.horarios.forEach((h) => {
      this.horarios.push(
        this.fb.group({
          dia: h.dia,
          horaInicio: h.horaInicio,
          horaFin: h.horaFin
        })
      );
    });
  }

  cancelarEdicion() {
    this.editar = false;
    this.medicoSeleccionadoId = null;
    this.emailBusquedaUsuario = '';
    this.form.reset({ estado: 'ACTIVO', tarifaConsulta: 0, disponible: null });
    this.horarios.clear();
    this.mensajeError = null;
  }

  guardar() {
    if (this.form.invalid) return;

    this.mensajeError = null;
    const medicoData = this.form.value;

    if (this.editar && this.medicoSeleccionadoId) {
      this.medicoService.actualizar(this.medicoSeleccionadoId, medicoData).subscribe({
        next: () => {
          this.listarMedicos();
          this.cancelarEdicion();
        },
        error: (err) => {
          console.error(err);
          this.mensajeError = err.error?.message || 'Error al actualizar médico';
        }
      });
    } else {
      this.medicoService.agregar(medicoData).subscribe({
        next: () => {
          this.listarMedicos();
          this.cancelarEdicion();
        },
        error: (err) => {
          console.error(err);
          this.mensajeError = err.error?.message || 'Error al agregar médico';
        }
      });
    }
  }
}
