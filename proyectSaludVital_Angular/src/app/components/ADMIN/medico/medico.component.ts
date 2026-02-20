import { Component, OnInit } from '@angular/core';
import { MedicoService } from '../../../services/medico.service';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ObtenerMedicoDTO } from '../../../DTO/obtener-medico-dto';
import { MedicoConMostrar } from '../../../DTO/medico-horario';

@Component({
  selector: 'app-medico',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './medico.component.html',
  styleUrl: './medico.component.css'
})
export class MedicoComponent implements OnInit {
  medicos: MedicoConMostrar[] = [];
  form: FormGroup;
  editar: boolean = false;
  medicoSeleccionadoId: number | null = null;
  mensajeError: string | null = null;

  constructor(
    private medicoService: MedicoService,
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
      disponible:[null,Validators.required],
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
    this.medicoService.listar().subscribe(data => {
      this.medicos = data;
    });
  }

  agregarHorario() {
    this.horarios.push(this.fb.group({
      dia: ['', Validators.required],
      horaInicio: ['', Validators.required],
      horaFin: ['', Validators.required]
    }));
  }

  eliminarHorario(index: number) {
    this.horarios.removeAt(index);
  }

  editarMedico(medico: ObtenerMedicoDTO) {
    this.editar = true;
    this.medicoSeleccionadoId = medico.id || null;

    // Rellenar formulario
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

    // Limpiar horarios anteriores
    this.horarios.clear();

    // Agregar horarios del médico
    medico.horarios.forEach(h => {
      this.horarios.push(this.fb.group({
        dia: h.dia,
        horaInicio: h.horaInicio,
        horaFin: h.horaFin
      }));
    });
  }

  guardar() {
  if (this.form.invalid) return;

  this.mensajeError = null; // limpiar errores previos
  const medicoData = this.form.value;

  if (this.editar && this.medicoSeleccionadoId) {
    // Actualizar médico
    this.medicoService.actualizar(this.medicoSeleccionadoId, medicoData).subscribe({
      next: res => {
        console.log('Médico actualizado', res);
        this.listarMedicos();
        this.form.reset();
        this.horarios.clear();
        this.editar = false;
        this.medicoSeleccionadoId = null;
      },
      error: err => {
        console.error(err);
        this.mensajeError = err.error?.message || 'Error al actualizar médico';
      }
    });
  } else {
    // Agregar médico nuevo
    this.medicoService.agregar(medicoData).subscribe({
      next: res => {
        console.log('Médico agregado', res);
        this.listarMedicos();
        this.form.reset();
        this.horarios.clear();
      },
      error: err => {
        console.error(err);
        this.mensajeError = err.error?.message || 'Error al agregar médico';
      }
    });
  }
}
}