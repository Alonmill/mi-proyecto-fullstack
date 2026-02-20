import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { PacienteService } from '../../../services/paciente.service';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-medico-layout',
  standalone: true,
  imports: [CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule],
  templateUrl: './medico-layout.component.html',
  styleUrl: './medico-layout.component.css'
})
export class MedicoLayoutComponent implements OnInit {
  pacientes: any[] = [];

  constructor(private pacienteService: PacienteService) {}

  ngOnInit(): void {
  console.log('ngOnInit MedicoLayout ejecutado'); // <- prueba inicial

  this.pacienteService.listar().subscribe({
    next: data => {
      console.log('Pacientes cargados:', data); 
      this.pacientes = data;
    },
    error: err => console.error('Error al cargar pacientes:', err)
  });
}
}

