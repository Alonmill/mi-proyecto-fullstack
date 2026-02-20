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


  constructor(private fb: FormBuilder, private citaService: CitaService, private medicoService: MedicoService) {
    this.form = this.fb.group({
      idPaciente: [null, Validators.required],
      idMedico: [null, Validators.required],
      fecha: ['', Validators.required],
      hora: ['', Validators.required],
      motivo: ['', Validators.required]
    });
  }


  ngOnInit(): void {
  this.medicoService.listar().subscribe({
    next: data => {
      console.log("MEDICOS QUE LLEGAN:", data); // 👈 AGREGA ESTO
      this.medicos = data;
    },
    error: err => {
      console.error('Error cargando médicos', err);
    }
  });
}

  onSubmit() {
    if (this.form.valid) {
      const dto: CrearCitaDTO = this.form.value;
      this.citaService.crear(dto).subscribe({
        next: () => {
          this.mensaje = '✅ Cita creada correctamente';
      
          this.form.reset();
        },
        error: err => {
  console.error('Error al crear cita', err);

  if (err.error && typeof err.error === 'string') {

    this.mensaje = '❌ ' + err.error;
  } else if (err.error && err.error.message) {
    this.mensaje = '❌ ' + err.error.message;
  } else {
    this.mensaje = '❌ Error desconocido al crear cita';
  }
}
      });
    }
  }
}