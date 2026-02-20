import { Component } from '@angular/core';
import { PacienteConMostrar } from '../../../DTO/paciente-enfermedad-alergia';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { PacienteService } from '../../../services/paciente.service';
import { ObtenerPacienteDTO } from '../../../DTO/obtener-paciente-dto';

@Component({
  selector: 'app-paciente',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './paciente.component.html',
  styleUrl: './paciente.component.css'
})
export class PacienteComponent {
    pacientes: PacienteConMostrar[] = [];
    form: FormGroup;
    editar: boolean = false;
    pacienteSeleccionadoId: number | null = null;
    mensajeError: string | null = null; 

  constructor(
      private pacienteService: PacienteService,
      private fb: FormBuilder
    ){
    this.form = this.fb.group({
  nombre: ['', Validators.required],
  numeroIdentificacion: [
    '',
    [Validators.required, Validators.pattern(/^\d{8}$/)] // solo 8 dígitos
  ],
  fechaNacimiento: ['', Validators.required],
  alergias: this.fb.array([]),
  enfermedades: this.fb.array([]),
  usuarioId: [null, [Validators.required, Validators.pattern(/^\d+$/)]] // solo números
});
}

  ngOnInit(): void {
    this.listarPacientes();
  }

  get alergias(): FormArray{
    return this.form.get('alergias') as FormArray;
  }

  get enfermedades(): FormArray{
    return this.form.get('enfermedades') as FormArray;
  }

  listarPacientes() {
    this.pacienteService.listar().subscribe(data => {
      this.pacientes = data;
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

  // --- Alergias ---
  this.alergias.clear();
  paciente.alergias.forEach(a => {
    // a es un string directamente
    this.alergias.push(this.fb.group({ nombre: [a, Validators.required] }));
  });

  // --- Enfermedades ---
  this.enfermedades.clear();
  paciente.enfermedades.forEach(e => {
    // e es un string directamente
    this.enfermedades.push(this.fb.group({ nombre: [e, Validators.required] }));
  });
}



   guardar() {
  if (this.form.invalid) return;

  this.mensajeError = null; // limpiar errores previos

  const pacienteData = {
    ...this.form.value,
    alergias: this.form.value.alergias.map((a: any) => a.nombre),
    enfermedades: this.form.value.enfermedades.map((e: any) => e.nombre)
  };

  if (this.editar && this.pacienteSeleccionadoId) {
    this.pacienteService.actualizar(this.pacienteSeleccionadoId, pacienteData).subscribe({
      next: res => {
        console.log('Paciente actualizado', res);
        this.listarPacientes();
        this.form.reset();
        this.alergias.clear();
        this.enfermedades.clear();
        this.editar = false;
        this.pacienteSeleccionadoId = null;
      },
      error: err => {
        console.error(err);
        this.mensajeError = err.error?.message || 'Error al actualizar paciente';
      }
    });
  } else {
    this.pacienteService.agregar(pacienteData).subscribe({
      next: res => {
        console.log('Paciente agregado', res);
        this.listarPacientes();
        this.form.reset();
        this.alergias.clear();
        this.enfermedades.clear();
      },
      error: err => {
        console.error(err);
        this.mensajeError = err.error?.message || 'Error al agregar paciente';
      }
    });
  }
}


}