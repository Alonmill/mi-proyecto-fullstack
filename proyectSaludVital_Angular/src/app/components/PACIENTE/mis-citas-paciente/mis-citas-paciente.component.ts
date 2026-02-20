import { Component, OnInit } from '@angular/core';

import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { CitaService } from '../../../services/cita.service';

@Component({
  selector: 'app-mis-citas-paciente',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './mis-citas-paciente.component.html',
  styleUrl: './mis-citas-paciente.component.css'
})
export class MisCitasPacienteComponent implements OnInit {
  citas: any[] = []; // <-- agregamos la propiedad para la tabla

  constructor(private citaService: CitaService) {}



  ngOnInit(): void {
    this.citaService.misCitas().subscribe({
      next: data => this.citas = data,
      error: err => console.error('Error al obtener mis citas', err)
    });
  }
}
