import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ObtenerPacienteDTO } from '../../../DTO/obtener-paciente-dto';
import { PacienteService } from '../../../services/paciente.service';

@Component({
  selector: 'app-perfil-paciente',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './perfil-paciente.component.html',
  styleUrl: './perfil-paciente.component.css'
})
export class PerfilPacienteComponent implements OnInit {

  paciente: ObtenerPacienteDTO | null = null;
  form: FormGroup;
  error: string = '';
 editar: boolean = false;
    pacienteSeleccionadoId: number | null = null;
  constructor(
        private pacienteService: PacienteService,
        private fb: FormBuilder
      ){
      this.form = this.fb.group({
      nombre: ['', Validators.required],
      numeroIdentificacion: [ '',   [ Validators.required, Validators.pattern(/^\d{8}$/) ] ],
      fechaNacimiento: ['', Validators.required],
      alergias: this.fb.array([]), 
      enfermedades: this.fb.array([]), 
      usuarioId: [null, Validators.required] 
  });
  }

  ngOnInit(): void {
    this.pacienteService.getPerfil().subscribe({
      next: data => this.paciente = data,
      error: err => {
        console.error(err);
        this.error = 'No tienes permiso o hubo un error al cargar el perfil';
      }
    });
  }

get alergias(): FormArray{
    return this.form.get('alergias') as FormArray;
  }

  get enfermedades(): FormArray{
    return this.form.get('enfermedades') as FormArray;
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

  cancelarEdicion() {
  this.editar = false;
  this.form.reset();
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

  // Convertimos alergias y enfermedades a arrays de strings
  const pacienteData = {
    ...this.form.value,
    alergias: this.form.value.alergias.map((a: any) => a.nombre),
    enfermedades: this.form.value.enfermedades.map((e: any) => e.nombre)
  };

  if (this.editar && this.pacienteSeleccionadoId) {
    // Actualizar paciente
    this.pacienteService.actualizar(this.pacienteSeleccionadoId, pacienteData).subscribe({
      next: res => {
        console.log('Paciente actualizado', res);

        // --- REFRESCAR PERFIL ---
        this.pacienteService.getPerfil().subscribe({
          next: data => this.paciente = data,
          error: err => console.error('Error al refrescar perfil', err)
        });

        // Limpiar formulario y salir de modo edición
        this.form.reset();
        this.alergias.clear();
        this.enfermedades.clear();
        this.editar = false;
        this.pacienteSeleccionadoId = null;
      },
      error: err => console.error(err)
    });
  } else {
    // Agregar paciente nuevo (si alguna vez lo necesitas)
    this.pacienteService.agregar(pacienteData).subscribe({
      next: res => {
        console.log('Paciente agregado', res);
        this.form.reset();
        this.alergias.clear();
        this.enfermedades.clear();
      },
      error: err => console.error(err)
    });
  }
}



}